package com.sunway.course.timetable.akka.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.List;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.akka.actor.record.VenueAccepted;

public class VenueCoordinatorActor extends AbstractBehavior<VenueCoordinatorActor.VenueCoordinatorCommand>
    implements VenueActor.VenueResponse {

    public interface VenueCoordinatorCommand {}

    public static final class RequestVenueAssignment implements VenueCoordinatorCommand {
        public final int durationHours;
        public final int minCapacity;
        public final ActorRef<SessionAssignmentActor.SessionAssignmentCommand> replyTo;

        public RequestVenueAssignment(int durationHours, int minCapacity,
                                      ActorRef<SessionAssignmentActor.SessionAssignmentCommand> replyTo) {
            this.durationHours = durationHours;
            this.minCapacity = minCapacity;
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

    private final List<ActorRef<VenueActor.VenueCommand>> venueActors;
    private final List<Venue> venues; // parallel list

    public static Behavior<VenueCoordinatorCommand> create(List<Venue> venues,
                                                          List<ActorRef<VenueActor.VenueCommand>> venueActors) {
        return Behaviors.setup(context -> new VenueCoordinatorActor(context, venues, venueActors));
    }

    private final ActorRef<VenueActor.VenueResponse> venueResponseAdapter;

    private VenueCoordinatorActor(ActorContext<VenueCoordinatorCommand> context,
                              List<Venue> venues,
                              List<ActorRef<VenueActor.VenueCommand>> venueActors) {
        super(context);
        this.venues = venues;
        this.venueActors = venueActors;

        // Message adapter: converts VenueResponse to VenueCoordinatorCommand
        this.venueResponseAdapter = context.messageAdapter(
            VenueActor.VenueResponse.class,
            response -> {
                if (response instanceof VenueActor.VenueAccepted accepted) {
                    return new VenueAcceptedMsg(accepted);
                } else if (response instanceof VenueActor.VenueRejected rejected) {
                    return new VenueRejectedMsg(rejected);
                } else {
                    // Handle unexpected message if needed
                    return null;
                }
            });
    }

    private record AssignmentState(RequestVenueAssignment request,
                                   int dayIndex,
                                   int startIndex,
                                   int durationSlots,
                                   int venueIndex) {}

    private AssignmentState currentAssignment;

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
        if (currentAssignment == null) return;

        var req = currentAssignment.request;
        int day = currentAssignment.dayIndex;
        int start = currentAssignment.startIndex;
        int dur = currentAssignment.durationSlots;
        int venueIdx = currentAssignment.venueIndex;

        while (day < 5) { // for 5 weekdays
            while (venueIdx < venues.size()) {
                Venue venue = venues.get(venueIdx);
                if (venue.getCapacity() < req.minCapacity) {
                    venueIdx++;
                    continue;
                }
                // check time bounds (assuming max 20 slots per day)
                if (start + dur > 20) {
                    start = 0;
                    day++;
                    continue;
                }

                ActorRef<VenueActor.VenueCommand> venueActor = venueActors.get(venueIdx);

                // Ask venue actor to check and assign slot,
                // pass this coordinator actor as replyTo
                venueActor.tell(new VenueActor.CheckAndAssignSlot(day, start, start + dur, venueResponseAdapter));


                currentAssignment = new AssignmentState(req, day, start, dur, venueIdx);
                return; // wait for reply
            }
            venueIdx = 0;
            day++;
        }

        // No slot found
        currentAssignment.request.replyTo.tell(
            new SessionAssignmentActor.SessionAssignmentFailed("No venue/time available"));
        currentAssignment = null;
    }

    private Behavior<VenueCoordinatorCommand> onVenueAccepted(VenueAcceptedMsg msg) {
        if (currentAssignment == null) return this;
        VenueActor.VenueAccepted accepted = msg.accepted;
        currentAssignment.request.replyTo.tell(
            new SessionAssignmentActor.SessionAssigned(
                accepted.venue,
                accepted.dayIndex,
                accepted.startIndex,
                accepted.durationSlots));
        currentAssignment = null;
        return this;
    }



    private Behavior<VenueCoordinatorCommand> onVenueRejected(VenueRejectedMsg msg) {
        if (currentAssignment == null) return this;

        // Try next time slot or venue
        int day = currentAssignment.dayIndex;
        int start = currentAssignment.startIndex + 1; // move 30 mins forward
        int venueIdx = currentAssignment.venueIndex + 1;

        if (start + currentAssignment.durationSlots > 20) {
            start = 0;
            day++;
        }
        if (venueIdx >= venues.size()) {
            venueIdx = 0;
            day++;
        }
        if (day >= 5) {
            currentAssignment.request.replyTo.tell(
                new SessionAssignmentActor.SessionAssignmentFailed("No venue/time available"));
            currentAssignment = null;
        } else {
            currentAssignment = new AssignmentState(currentAssignment.request, day, start, currentAssignment.durationSlots, venueIdx);
            tryNextAssignment();
        }
        return this;
    }
}
