package com.sunway.course.timetable.akka.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import com.sunway.course.timetable.model.Venue;

public class SessionAssignmentActor extends AbstractBehavior<SessionAssignmentActor.SessionAssignmentCommand> {

    public interface SessionAssignmentCommand {}

    public static final class AssignSession implements SessionAssignmentCommand {
        public final int durationHours;
        public final int minCapacity;
        public final ActorRef<SessionAssignmentCommand> replyTo;  // Actor expects commands here
        public final ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> coordinator;

        public AssignSession(int durationHours, int minCapacity,
                             ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> coordinator,
                             ActorRef<SessionAssignmentCommand> replyTo) {
            this.durationHours = durationHours;
            this.minCapacity = minCapacity;
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
        // Forward the request to the VenueCoordinatorActor, passing self as replyTo
        msg.coordinator.tell(new VenueCoordinatorActor.RequestVenueAssignment(
            msg.durationHours, msg.minCapacity, context.getSelf()));
        return this;
    }

    private Behavior<SessionAssignmentCommand> onSessionAssigned(SessionAssigned msg) {
        context.getLog().info("Session assigned: Venue={} Day={} StartSlot={}",
                              msg.venue.getName(), msg.dayIndex, msg.startIndex);
        // Notify whoever asked for assignment - if you saved ActorRef for this, tell it
        // For example:
        // msg.replyTo.tell(msg); // if you kept track of who asked

        return this;
    }

    private Behavior<SessionAssignmentCommand> onSessionAssignmentFailed(SessionAssignmentFailed msg) {
        context.getLog().warn("Session assignment failed: {}", msg.reason);
        // Notify requester if you saved ActorRef

        return this;
    }
}

