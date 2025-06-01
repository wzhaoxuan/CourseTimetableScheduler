package com.sunway.course.timetable.akka.actor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class VenueCoordinatorActor extends AbstractBehavior<VenueCoordinatorActor.VenueCoordinatorCommand>
    implements VenueActor.VenueResponse {
    
    private static final Logger log = LoggerFactory.getLogger(VenueCoordinatorActor.class);
    
    // === Constants ===
    private static final int MAX_DAYS = 5;
    private static final int MAX_SLOTS_PER_DAY = 20;

    public interface VenueCoordinatorCommand {}

    public static final class InitializeVenues implements VenueCoordinatorCommand {
        public final List<Venue> venues;
        public final List<ActorRef<VenueActor.VenueCommand>> venueActors;

        public InitializeVenues(List<Venue> venues, List<ActorRef<VenueActor.VenueCommand>> venueActors) {
            this.venues = venues;
            this.venueActors = venueActors;
        }
    }

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
    private final VenueAvailabilityMatrix venueAvailability;
    private final ActorRef<VenueActor.VenueResponse> venueResponseAdapter;
    private AssignmentState currentAssignment;

    private record AssignmentState(RequestVenueAssignment request,
                                   int dayIndex,
                                   int startIndex,
                                   int durationSlots,
                                   int venueIndex) {}

    // === Factory Method ===
    public static Behavior<VenueCoordinatorCommand> create(List<Venue> venues,
                                                           List<ActorRef<VenueActor.VenueCommand>> venueActors,
                                                           LecturerAvailabilityMatrix lecturerAvailability,
                                                           VenueAvailabilityMatrix venueAvailability) {
        return Behaviors.setup(context -> new VenueCoordinatorActor(context,
            venues,
            venueActors,
            lecturerAvailability,
            venueAvailability));
    }


    private VenueCoordinatorActor(ActorContext<VenueCoordinatorCommand> context,
                                  List<Venue> venues,
                                  List<ActorRef<VenueActor.VenueCommand>> venueActors,
                                  LecturerAvailabilityMatrix lecturerAvailability,
                                  VenueAvailabilityMatrix venueAvailability) {
        super(context);
        this.venues = venues;
        this.venueActors = venueActors;
        this.lecturerAvailability = lecturerAvailability;
        this.venueAvailability = venueAvailability;

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
        // getContext().getLog().info("Venue Actor Size:{}, Venue Size: {}",
        //         venueActors.size(), venues.size());
        
        if (venueActors.size() != venues.size()) {
            log.warn("Venue list or venueActors list empty or mismatched! Cannot perform venue assignment.");
        }

        
        if (venues.isEmpty()) {
            log.warn("No venues available for assignment.");
            msg.replyTo.tell(new SessionAssignmentActor.SessionAssignmentFailed("No venues available"));
            currentAssignment = null;
            return this;
        }
        
        int durationSlots = msg.durationHours * 2; // assuming 30-min slots, 2 slots per hour
        if (durationSlots > MAX_SLOTS_PER_DAY) {
            msg.replyTo.tell(new SessionAssignmentActor.SessionAssignmentFailed("Session too long for one day"));
            return this;
        }
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

            // getContext().getLog().warn("Day {}, Max Days {}", day, MAX_DAYS);
            // getContext().getLog().warn("Start {}, Max Slots {}", start, MAX_SLOTS_PER_DAY);
            // getContext().getLog().warn("VenueIdx {}, Total Venues {}", venueIdx, venues.size());
            // getContext().getLog().warn("Duration Slots {}, Total Slots {}", durationSlots, MAX_SLOTS_PER_DAY);
            // getContext().getLog().info("Trying to assign: {} hours, min capacity {}, lecturer {}",
            //         req.durationHours, req.minCapacity, req.lecturerId);

            if (day >= MAX_DAYS) {
                getContext().getLog().warn("Day {} exceeds maximum {}", day, MAX_DAYS);
                req.replyTo.tell(new SessionAssignmentActor.SessionAssignmentFailed("No days available"));
                currentAssignment = null;
                return;
            }

            if (start + durationSlots > MAX_SLOTS_PER_DAY) {
                // Move to next day if current start + duration exceeds slots per day
                day++;
                start = 0;
                venueIdx = 0;
                currentAssignment = new AssignmentState(req, day, start, durationSlots, venueIdx);
                continue;
            }

            if (venueIdx >= venues.size()) {
                // Move to next start time if all venues exhausted for current start and day
                venueIdx = 0;
                start++;
                currentAssignment = new AssignmentState(req, day, start, durationSlots, venueIdx);
                continue;
            }

            Venue venue = venues.get(venueIdx);
            if (venue.getCapacity() < req.minCapacity) {
                getContext().getLog().info("Skippping venue '{}' (capacity{}) < required {}",
                        venue.getName(), venue.getCapacity(), req.minCapacity);
                currentAssignment = new AssignmentState(req, day, start, durationSlots, venueIdx + 1);
                continue;
            }

            if (!lecturerAvailability.isAvailable(req.lecturerId, day, start, start + durationSlots)) {
                // getContext().getLog().info("Lecturer '{}' not available on day {} from slot {}-{}",
                //         req.lecturerId, day, start, start + durationSlots);
                currentAssignment = new AssignmentState(req, day, start, durationSlots, venueIdx + 1);
                continue;
            }

            if (!venueAvailability.isAvailable(
                    venue, start, start + durationSlots, day)) {
                currentAssignment = new AssignmentState(req, day, start, durationSlots, venueIdx + 1);
                continue;
            }

            // getContext().getLog().info("Trying venue '{}' on day {} from slot {} to {}",
            //         venue.getName(), day, start, start + durationSlots);

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
        var req = currentAssignment.request;

         // Update availability matrices
        lecturerAvailability.assign(req.lecturerId,
            accepted.dayIndex, accepted.startIndex, accepted.startIndex + accepted.durationSlots);
        venueAvailability.assign(
            accepted.venue,
            accepted.startIndex, accepted.startIndex + accepted.durationSlots, accepted.dayIndex);

        // Reply to session actor
        req.replyTo.tell(new SessionAssignmentActor.SessionAssigned(
            accepted.venue, accepted.dayIndex, accepted.startIndex, accepted.durationSlots));
        

        // getContext().getLog().info("Assigned venue '{}' on day {} from slot {} to {}",
        //     accepted.venue.getName(), accepted.dayIndex, accepted.startIndex, accepted.startIndex + accepted.durationSlots);

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
            currentAssignment.durationSlots,
            currentAssignment.venueIndex + 1
        );

        tryNextAssignment();
        return this;
    }
}
