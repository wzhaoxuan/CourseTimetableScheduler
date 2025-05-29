package com.sunway.course.timetable.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.ActorContext;
import akka.pattern.StatusReply;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.actor.SubjectLecturerActor;
import java.time.LocalTime;

import java.util.List;

public class SubjectLecturerActor extends AbstractBehavior<SubjectLecturerActor.Command> {

    // Define a marker interface for all messages
    public interface Command {}

    // A message type that checks for session conflicts
    public static class CheckConflict implements Command {
        public final Session session;
        public final ActorRef<StatusReply<Boolean>> replyTo;

        public CheckConflict(Session session, ActorRef<StatusReply<Boolean>> replyTo) {
            this.session = session;
            this.replyTo = replyTo;
        }
    }

    // Factory method to create the actor
    public static Behavior<Command> create(List<Session> scheduledSessions) {
        return Behaviors.setup(ctx -> new SubjectLecturerActor(ctx, scheduledSessions));
    }

    private final List<Session> scheduledSessions;

    // Private constructor
    private SubjectLecturerActor(ActorContext<Command> context, List<Session> scheduledSessions) {
        super(context);
        this.scheduledSessions = scheduledSessions;
        getContext().getLog().info("SubjectLecturerActor Started");
    }

    // Receive method that handles messages
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(CheckConflict.class, this::onCheckConflict)
                .build();
    }

    // Message handler
    private Behavior<Command> onCheckConflict(CheckConflict msg) {
        getContext().getLog().info("Checking conflict for session: {}", msg.session);
        Session newSession = msg.session;
        boolean conflict = scheduledSessions.stream().anyMatch(existing ->
            existing.getDay().equals(newSession.getDay()) &&
            timesOverlap(existing.getStartTime(), existing.getEndTime(),
                        newSession.getStartTime(), newSession.getEndTime()) &&
            existing.getLecturer().equals(newSession.getLecturer())
        );


        msg.replyTo.tell(StatusReply.success(conflict));
        return this;
    }

    private boolean timesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
