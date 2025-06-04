package com.sunway.course.timetable.akka.actor;
import java.util.List;

import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class SessionAssignmentActor extends AbstractBehavior<SessionAssignmentActor.SessionAssignmentCommand> {

    public interface SessionAssignmentCommand {}

    public static final class AssignSession implements SessionAssignmentCommand {
        public final int durationHours;
        public final int minCapacity;
        public final String lecturerName; 
        public final Module module;
        public final List<Student> eligibleStudents; // List of students to assign to this session
        public final String sessionType;
        public final int groupIndex;
        public final int groupCount;
        public final ActorRef<SessionAssignmentResult> replyTo;
        public final ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> coordinator;
        public final List<String> preferredVenues;

        public AssignSession(int durationHours, int minCapacity, String lecturerName,
                            Module module, List<Student> eligibleStudents,
                            String sessionType, int groupIndex, int groupCount,
                            ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> coordinator,
                            ActorRef<SessionAssignmentResult> replyTo,
                            List<String> preferredVenues) {
            this.durationHours = durationHours;
            this.minCapacity = minCapacity;
            this.lecturerName = lecturerName;
            this.module = module;
            this.eligibleStudents = eligibleStudents;
            this.sessionType = sessionType;
            this.groupIndex = groupIndex;
            this.groupCount = groupCount;
            this.coordinator = coordinator;
            this.replyTo = replyTo;
            this.preferredVenues = preferredVenues;
        }
    }

    public interface SessionAssignmentResult extends SessionAssignmentCommand {}

    // Responses from VenueCoordinatorActor come in as commands here:
    public static final class SessionAssigned implements SessionAssignmentResult {
        public final Venue venue;
        public final int dayIndex;
        public final int startIndex;
        public final int durationSlots;
        public final List<Student> assignedStudents;


        public SessionAssigned(Venue venue, int dayIndex, int startIndex, int durationSlots,
                               List<Student> assignedStudents) {
            this.venue = venue;
            this.dayIndex = dayIndex;
            this.startIndex = startIndex;
            this.durationSlots = durationSlots;
            this.assignedStudents = assignedStudents;
            
        }

        public List<Student> getAssignedStudents() {
            return assignedStudents;
        }
    }


    public static final class SessionAssignmentFailed implements SessionAssignmentResult {
        public final String reason;

        public SessionAssignmentFailed(String reason) { this.reason = reason; }
    }

    private final ActorContext<SessionAssignmentCommand> context;
    private ActorRef<SessionAssignmentResult> originalRequester;

    public static Behavior<SessionAssignmentCommand> create() {
        return Behaviors.setup(SessionAssignmentActor::new);
    }

    private SessionAssignmentActor(ActorContext<SessionAssignmentCommand> context) {
        super(context);
        this.context = context;
    }

    @Override
    public Receive<SessionAssignmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(AssignSession.class, this::onAssignSession)
                .onMessage(SessionAssigned.class, this::onSessionAssigned)
                .onMessage(SessionAssignmentFailed.class, this::onSessionAssignmentFailed)
                .build();
    }

    private Behavior<SessionAssignmentCommand> onAssignSession(AssignSession msg) {
        // Save original requester to forward results later
        this.originalRequester = msg.replyTo;

        // context.getLog().info("Received AssignSession request: duration={} hours, minCapacity={}, lecturer={}",
        //         msg.durationHours, msg.minCapacity, msg.lecturerName);

        // Forward the request to the VenueCoordinatorActor, passing self as replyTo
        msg.coordinator.tell(new VenueCoordinatorActor.RequestVenueAssignment(
            msg.durationHours, msg.minCapacity, msg.lecturerName, msg.module, msg.eligibleStudents,
            msg.sessionType, msg.groupIndex, msg.groupCount, context.getSelf()));

        return this;
    }

    private Behavior<SessionAssignmentCommand> onSessionAssigned(SessionAssigned msg) {
        // context.getLog().info("Session assigned: Venue={} Day={} StartSlot={}",
        //                       msg.venue.getName(), msg.dayIndex, msg.startIndex);

        if (originalRequester != null) {
            originalRequester.tell(msg); // Notify original requester
        }
        // Reset for next request (if any)
        originalRequester = null;

        return this;
    }

    private Behavior<SessionAssignmentCommand> onSessionAssignmentFailed(SessionAssignmentFailed msg) {
        context.getLog().warn("Session assignment failed: {}", msg.reason);

        if (originalRequester != null) {
            originalRequester.tell(msg); // Notify original requester
        }
        originalRequester = null;

        return this;
    }
    
}

