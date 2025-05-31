package com.sunway.course.timetable.akka.actor;

import java.util.List;

import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class VenueCoordinatorActor extends AbstractBehavior<VenueCoordinatorActor.VenueCoordinatorCommand>
    implements VenueActor.VenueResponse {

    // === Constants ===
    private static final int MAX_DAYS = 5;
    private static final int MAX_SLOTS_PER_DAY = 20;

    public interface VenueCoordinatorCommand {}

    public static final class RequestVenueAssignment implements VenueCoordinatorCommand {
        public final int durationHours;
        public final int minCapacity;
        public final String lecturerId;
        public final ActorRef<SessionAssignmentActor.SessionAssignmentCommand> replyTo;

        public RequestVenueAssignment(int durationHours, int minCapacity, String lecturerId,
                                  ActorRef<SessionAssignmentActor.SessionAssignmentCommand> replyTo) {
            this.durationHours = durationHours;
            this.minCapacity = minCapacity;
            this.lecturerId = lecturerId;
            this.replyTo = replyTo;
        }
    }

    public static final class VenueAssignmentAttempt implements VenueCoordinatorCommand {
        public final Venue venue;
        public final int dayIndex;
        public final int startIndex;
        public final int durationSlots;
        public final ActorRef<VenueActor.VenueResponse> replyTo;

        public VenueAssignmentAttempt(Venue venue, int dayIndex, int startIndex, int durationSlots,
                                      ActorRef<VenueActor.VenueResponse> replyTo) {
            this.venue = venue;
            this.dayIndex = dayIndex;
            this.startIndex = startIndex;
            this.durationSlots = durationSlots;
            this.replyTo = replyTo;
        }
    }

    public static final class VenueAcceptedMsg implements VenueCoordinatorCommand {
        public final VenueActor.VenueAccepted accepted;

        public VenueAcceptedMsg(VenueActor.VenueAccepted accepted) {
            this.accepted = accepted;
        }
    }

    public static final class VenueRejectedMsg implements VenueCoordinatorCommand {
        public final VenueActor.VenueRejected rejected;

        public VenueRejectedMsg(VenueActor.VenueRejected rejected) {
            this.rejected = rejected;
        }
    }

     // === Internal State ===
    private final List<ActorRef<VenueActor.VenueCommand>> venueActors;
    private final List<Venue> venues; // parallel list
    private final LecturerAvailabilityMatrix lecturerAvailability;
    private final ActorRef<VenueActor.VenueResponse> venueResponseAdapter;

    private record AssignmentState(RequestVenueAssignment request,
                                   int dayIndex,
                                   int startIndex,
                                   int durationSlots,
                                   int venueIndex) {}

    private AssignmentState currentAssignment;

    // === Factory Method ===
    public static Behavior<VenueCoordinatorCommand> create(List<Venue> venues,
                                                          List<ActorRef<VenueActor.VenueCommand>> venueActors,
                                                          LecturerAvailabilityMatrix lecturerAvailability) {
        return Behaviors.setup(context -> new VenueCoordinatorActor(context, venues, venueActors, lecturerAvailability));
    }


    private VenueCoordinatorActor(ActorContext<VenueCoordinatorCommand> context,
                              List<Venue> venues,
                              List<ActorRef<VenueActor.VenueCommand>> venueActors,
                              LecturerAvailabilityMatrix lecturerAvailability) {
        super(context);
        this.venues = venues;
        this.venueActors = venueActors;
        this.lecturerAvailability = lecturerAvailability;

        // Message adapter: converts VenueResponse to VenueCoordinatorCommand
        this.venueResponseAdapter = context.messageAdapter(
            VenueActor.VenueResponse.class,
            response -> {
                if (response instanceof VenueActor.VenueAccepted accepted) {
                    return new VenueAcceptedMsg(accepted);
                } else if (response instanceof VenueActor.VenueRejected rejected) {
                    return new VenueRejectedMsg(rejected);
                } else {
                    getContext().getLog().warn("Unhandled VenueResponse: {}", response);
                    return null;
                }
            });
    }

    // === Message Handlers ===
    @Override
    public Receive<VenueCoordinatorCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(RequestVenueAssignment.class, this::onRequestVenueAssignment)
                .onMessage(VenueAcceptedMsg.class, this::onVenueAccepted)
                .onMessage(VenueRejectedMsg.class, this::onVenueRejected)
                .build();
    }

    private Behavior<VenueCoordinatorCommand> onRequestVenueAssignment(RequestVenueAssignment msg) {
        int durationSlots = msg.durationHours * 2; // assuming 30-min slots, 2 slots per hour
        currentAssignment = new AssignmentState(msg, 0, 0, durationSlots, 0);
        tryNextAssignment();
        return this;
    }

    private void tryNextAssignment() {
        while (currentAssignment != null) {
            int day = currentAssignment.dayIndex;
            int start = currentAssignment.startIndex;
            int venueIdx = currentAssignment.venueIndex;
            int durationSlots = currentAssignment.durationSlots;
            var req = currentAssignment.request;

            if (day >= MAX_DAYS) {
                req.replyTo.tell(new SessionAssignmentActor.SessionAssignmentFailed("No venue/time available"));
                currentAssignment = null;
                return;
            }

            if (venueIdx >= venues.size()) {
                // Move to next time slot
                venueIdx = 0;
                start++;
                if (start + durationSlots > MAX_SLOTS_PER_DAY) {
                    start = 0;
                    day++;
                }
                currentAssignment = new AssignmentState(req, day, start, venueIdx, durationSlots);
                continue;
            }

            Venue venue = venues.get(venueIdx);
            if (venue.getCapacity() < req.minCapacity) {
                currentAssignment = new AssignmentState(req, day, start, venueIdx + 1, durationSlots);
                continue;
            }

            if (!lecturerAvailability.isAvailable(req.lecturerId, day, start, start + durationSlots)) {
                currentAssignment = new AssignmentState(req, day, start, venueIdx + 1, durationSlots);
                continue;
            }

            getContext().getLog().info("Trying venue '{}' on day {} from slot {} to {}",
                    venue.getName(), day, start, start + durationSlots);

            venueActors.get(venueIdx).tell(new VenueActor.CheckAndAssignSlot(
                day, start, start + durationSlots, req.lecturerId, venueResponseAdapter
            ));

            // Wait for venue response before continuing
            return;
        }
    }

    private Behavior<VenueCoordinatorCommand> onVenueAccepted(VenueAcceptedMsg msg) {
        if (currentAssignment == null) return this;

        var accepted = msg.accepted;
        currentAssignment.request.replyTo.tell(new SessionAssignmentActor.SessionAssigned(
            accepted.venue,
            accepted.dayIndex,
            accepted.startIndex,
            accepted.durationSlots
        ));

        getContext().getLog().info("Assigned venue '{}' on day {} from slot {} to {}",
            accepted.venue.getName(), accepted.dayIndex, accepted.startIndex, accepted.startIndex + accepted.durationSlots);

        currentAssignment = null;
        return this;
    }



    private Behavior<VenueCoordinatorCommand> onVenueRejected(VenueRejectedMsg msg) {
        if (currentAssignment == null) return this;

        // Try next venue for same time
        currentAssignment = new AssignmentState(
            currentAssignment.request,
            currentAssignment.dayIndex,
            currentAssignment.startIndex,
            currentAssignment.venueIndex + 1,
            currentAssignment.durationSlots
        );

        tryNextAssignment();
        return this;
    }
}
