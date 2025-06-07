package com.sunway.course.timetable.akka.actor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunway.course.timetable.engine.AC3DomainPruner.AssignmentOption;
import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
import com.sunway.course.timetable.singleton.StudentAvailabilityMatrix;
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
        public final String lecturerName;
        public final Module module;
        public final List<Student> eligibleStudents;
        public final String sessionType;
        public final int groupIndex;
        public final int groupCount;
        public final ActorRef<SessionAssignmentActor.SessionAssignmentCommand> replyTo;
        private final List<String> preferredVenues;
        private final List<AssignmentOption> prunedDomain;

        public RequestVenueAssignment(int durationHours, int minCapacity, String lecturerName,
                                        Module module, List<Student> eligibleStudents,
                                        String sessionType, 
                                        int groupIndex,
                                        int groupCount,
                                        ActorRef<SessionAssignmentActor.SessionAssignmentCommand> replyTo,
                                        List<String> preferredVenues,
                                        List<AssignmentOption> prunedDomain) {
            this.durationHours = durationHours;
            this.minCapacity = minCapacity;
            this.lecturerName = lecturerName;
            this.module = module;
            this.eligibleStudents = eligibleStudents;
            this.sessionType = sessionType;
            this.groupIndex = groupIndex;
            this.groupCount = groupCount;
            this.replyTo = replyTo;
            this.preferredVenues = preferredVenues;
            this.prunedDomain = prunedDomain;
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

    // === Dependencies ===
    private final List<ActorRef<VenueActor.VenueCommand>> venueActors;
    private final List<Venue> venues; // parallel list
    private final LecturerAvailabilityMatrix lecturerAvailability;
    private final VenueAvailabilityMatrix venueAvailability;
    private final StudentAvailabilityMatrix studentAvailability;
    private final ActorRef<VenueActor.VenueResponse> venueResponseAdapter;

    // === Internal State ===
    private List<AssignmentOption> domainQueue = new ArrayList<>();
    private int domainIndex = 0;
    private ActorRef<SessionAssignmentActor.SessionAssignmentCommand> currentRequester;
    private RequestVenueAssignment currentRequest;


    // === Factory Method ===
    public static Behavior<VenueCoordinatorCommand> create(List<Venue> venues,
                                                           List<ActorRef<VenueActor.VenueCommand>> venueActors,
                                                           LecturerAvailabilityMatrix lecturerAvailability,
                                                           VenueAvailabilityMatrix venueAvailability,
                                                           StudentAvailabilityMatrix studentAvailability) {
        return Behaviors.setup(context -> new VenueCoordinatorActor(context,
            venues,
            venueActors,
            lecturerAvailability,
            venueAvailability,
            studentAvailability));
    }


    private VenueCoordinatorActor(ActorContext<VenueCoordinatorCommand> context,
                                  List<Venue> venues,
                                  List<ActorRef<VenueActor.VenueCommand>> venueActors,
                                  LecturerAvailabilityMatrix lecturerAvailability,
                                  VenueAvailabilityMatrix venueAvailability,
                                  StudentAvailabilityMatrix studentAvailability) {
        super(context);
        this.venues = venues;
        this.venueActors = venueActors;
        this.lecturerAvailability = lecturerAvailability;
        this.venueAvailability = venueAvailability;
        this.studentAvailability = studentAvailability;

        // Message adapter: converts VenueResponse to VenueCoordinatorCommand
        this.venueResponseAdapter = context.messageAdapter(
            VenueActor.VenueResponse.class,
            response -> {
                if (response instanceof VenueActor.VenueAccepted accepted) return new VenueAcceptedMsg(accepted);
                if (response instanceof VenueActor.VenueRejected rejected) return new VenueRejectedMsg(rejected);
                getContext().getLog().warn("Unhandled VenueResponse: {}", response);
                return null;
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
        if (msg.prunedDomain == null || msg.prunedDomain.isEmpty()) {
            msg.replyTo.tell(new SessionAssignmentActor.SessionAssignmentFailed("No valid domain options"));
            return this;
        }

        // Sort pruned domain by preferred venue proximity (if applicable)
        List<AssignmentOption> sortedDomain = new ArrayList<>(msg.prunedDomain);
        if (msg.preferredVenues != null && !msg.preferredVenues.isEmpty()) {
             // Best-fit by capacity
            sortedDomain.sort(Comparator
                .comparingInt((AssignmentOption opt) -> {
                    int surplus = opt.venue().getCapacity() - msg.minCapacity;
                    return (surplus < 0) ? Integer.MAX_VALUE : surplus; // penalize too-small rooms
                })
                .thenComparing(opt -> opt.venue().getName()) // optional: stable tie-breaker
            );
        } else {
            sortedDomain.sort(Comparator.comparingInt(opt -> opt.venue().getCapacity()));
        }

        // Store for retry
        this.domainQueue = sortedDomain;
        this.domainIndex = 0;
        this.currentRequester = msg.replyTo;
        this.currentRequest = msg;

        return tryNextDomainOption();
    }

    private Behavior<VenueCoordinatorCommand> tryNextDomainOption() {
        while(domainIndex < domainQueue.size()){
            AssignmentOption option = domainQueue.get(domainIndex++);
            int venueIndex = venues.indexOf(option.venue());
            if (venueIndex == -1) continue;

            getContext().getLog().info("[TRY AC3] Day={} Slot={} Venue={}",
                option.day(), option.startSlot(), option.venue().getId());

            // Check if the venue is available
            venueActors.get(venueIndex).tell(new VenueActor.CheckAndAssignSlot(
                option.day(), option.startSlot(), 
                option.startSlot() + currentRequest.durationHours * 2,
                currentRequest.lecturerName, venueResponseAdapter
            ));

            return this; // Wait for response
        }

        currentRequester.tell(new SessionAssignmentActor.SessionAssignmentFailed(
            "No valid domain options"));
        return this;
    }

    private Behavior<VenueCoordinatorCommand> onVenueAccepted(VenueAcceptedMsg msg) {
        var accepted = msg.accepted;
        var req = currentRequest;

        // Mark availability
        lecturerAvailability.assign(req.lecturerName, accepted.dayIndex, accepted.startIndex, accepted.startIndex + accepted.durationSlots);
        venueAvailability.assign(accepted.venue, accepted.startIndex, accepted.startIndex + accepted.durationSlots, accepted.dayIndex);

        List<Student> allEligible = req.eligibleStudents;
        List<Student> assigned;

        if (req.sessionType.equalsIgnoreCase("lecture")) {
            assigned = allEligible.stream()
                .filter(s -> studentAvailability.isAvailable(s.getId(), accepted.dayIndex, accepted.startIndex, accepted.startIndex + accepted.durationSlots))
                .toList();
        } else {
            final int MAX_GROUP_SIZE = 35;
            assigned = allEligible.stream()
                .filter(s -> studentAvailability.isAvailable(s.getId(), accepted.dayIndex, accepted.startIndex, accepted.startIndex + accepted.durationSlots))
                .sorted(Comparator.comparingLong(Student::getId)) // Optional: stable assignment
                .limit(MAX_GROUP_SIZE)
                .toList();

        }

        if (assigned.isEmpty()) {
            currentRequester.tell(new SessionAssignmentActor.SessionAssignmentFailed("No students available for selected slot"));
            return this;
        }

        assigned.forEach(s -> studentAvailability.markUnavailable(s.getId(), accepted.dayIndex, accepted.startIndex, accepted.startIndex + accepted.durationSlots));

        currentRequester.tell(new SessionAssignmentActor.SessionAssigned(
            accepted.venue,
            accepted.dayIndex,
            accepted.startIndex,
            accepted.durationSlots,
            assigned
        ));

        return this;
    }

    private Behavior<VenueCoordinatorCommand> onVenueRejected(VenueRejectedMsg msg) {
        log.info("Venue rejected assignment, moving to next option.");
        return tryNextDomainOption();
    }

}