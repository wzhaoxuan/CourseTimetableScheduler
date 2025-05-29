package com.sunway.course.timetable.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.*;
import akka.pattern.StatusReply;

import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.assignment.AssignmentResult;


import java.util.List;

public class VenueCoordinatorActor extends AbstractBehavior<VenueCoordinatorActor.Command> {

    // Define a marker interface for all messages
    public interface Command {}

    public static class RequestVenue implements Command {
        public final Session session;
        public final ActorRef<SubjectLecturerActor.Command> lecturerActor;
        public final ActorRef<StatusReply<AssignmentResult>> replyTo;

        public RequestVenue(Session session, ActorRef<SubjectLecturerActor.Command> lecturerActor,
                            ActorRef<StatusReply<AssignmentResult>> replyTo) {
            this.session = session;
            this.lecturerActor = lecturerActor;
            this.replyTo = replyTo;
        }
    }


    // Factory method to create the actor
    public static Behavior<Command> create(List<Venue> venues) {
        return Behaviors.setup(ctx -> new VenueCoordinatorActor(ctx, venues));
    }

    private final List<Venue> venues;

    // Private constructor
    private VenueCoordinatorActor(ActorContext<Command> ctx, List<Venue> venues) {
        super(ctx);
        this.venues = venues;
        getContext().getLog().info("VenueCoordinatorActor Started");
    }

    // Receive method that handles messages
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(RequestVenue.class, this::onRequestVenue)
                .build();
    }

    // Message handler
    private Behavior<Command> onRequestVenue(RequestVenue msg) {
        // Implement logic:
        // 1. Check VenueAssignmentTable for availability
        // 2. Ask SubjectLecturerActor whether conflict exists (same lecturer or module)
        // 3. If yes, reject; if no, assign
        getContext().getLog().info("Received RequestVenue for session: {}", msg.session);
        
        //Spawn Child Worker
        getContext().spawnAnonymous(
            VenueAssignmentWorker.create(msg.session, venues, msg.lecturerActor, msg.replyTo)
        );
        return this;
    }

}

