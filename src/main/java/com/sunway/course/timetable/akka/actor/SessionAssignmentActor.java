package com.sunway.course.timetable.akka.actor;

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
        public final String lecturerId; 
        public final ActorRef<SessionAssignmentCommand> replyTo;
        public final ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> coordinator;

        public AssignSession(int durationHours, int minCapacity, String lecturerId,
                            ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> coordinator,
                            ActorRef<SessionAssignmentCommand> replyTo) {
            this.durationHours = durationHours;
            this.minCapacity = minCapacity;
            this.lecturerId = lecturerId;
            this.coordinator = coordinator;
            this.replyTo = replyTo;
        }
    }

    // Responses from VenueCoordinatorActor come in as commands here:
    public static final class SessionAssigned implements SessionAssignmentCommand {
        public final Venue venue;
        public final int dayIndex;
        public final int startIndex;
        public final int durationSlots;

        public SessionAssigned(Venue venue, int dayIndex, int startIndex, int durationSlots) {
            this.venue = venue;
            this.dayIndex = dayIndex;
            this.startIndex = startIndex;
            this.durationSlots = durationSlots;
        }
    }

    public static final class SessionAssignmentFailed implements SessionAssignmentCommand {
        public final String reason;

        public SessionAssignmentFailed(String reason) { this.reason = reason; }
    }

    private final ActorContext<SessionAssignmentCommand> context;
    private ActorRef<SessionAssignmentCommand> originalRequester;

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
        //         msg.durationHours, msg.minCapacity, msg.lecturerId);

        // Forward the request to the VenueCoordinatorActor, passing self as replyTo
        msg.coordinator.tell(new VenueCoordinatorActor.RequestVenueAssignment(
            msg.durationHours, msg.minCapacity, msg.lecturerId, context.getSelf()));

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

