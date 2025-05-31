package com.sunway.course.timetable;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.akka.actor.SessionAssignmentActor.SessionAssigned;
import com.sunway.course.timetable.akka.actor.SessionAssignmentActor.SessionAssignmentCommand;
import com.sunway.course.timetable.akka.actor.SessionAssignmentActor.SessionAssignmentFailed;
import com.sunway.course.timetable.akka.actor.VenueActor;
import com.sunway.course.timetable.akka.actor.VenueCoordinatorActor;
import com.sunway.course.timetable.model.Venue;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import akka.actor.typed.javadsl.Behaviors;
import jakarta.annotation.PostConstruct;

@Service
public class FullActorTest {

    private final ActorSystem<Void> system;
    private volatile boolean testRun = false;

    public FullActorTest(ActorSystem<Void> system) {
        this.system = system;
    }

    @PostConstruct
    public void runTest() {
        if (testRun) return;
        testRun = true;

        try {
            // === 1. Create venues and venue actors ===
            Venue venue = new Venue("Auditorium", "JC1", 234, "Level 1", "University");
            ActorRef<VenueActor.VenueCommand> venueActor = system.systemActorOf(
                    VenueActor.create(venue),
                    "VenueActor-" + UUID.randomUUID(),
                    Props.empty());

            List<Venue> venues = List.of(venue);
            List<ActorRef<VenueActor.VenueCommand>> venueActors = List.of(venueActor);

            // === 2. Create SessionAssignmentActor that logs result ===
            ActorRef<SessionAssignmentCommand> sessionAssignmentActor =
                system.systemActorOf(
                        Behaviors.setup(ctx ->
                                Behaviors.receive(SessionAssignmentCommand.class)
                                        .onMessage(SessionAssigned.class, msg -> {
                                            System.out.printf("Session assigned to %s on day %d, start %d, duration %d%n",
                                                    msg.venue.getName(), msg.dayIndex, msg.startIndex, msg.durationSlots);

                                            return Behaviors.stopped();
                                        })
                                        .onMessage(SessionAssignmentFailed.class, msg -> {
                                            System.out.printf("Session assignment failed: %s%n", msg.reason);

                                            return Behaviors.stopped();
                                        })
                                        .build()
                        ),
                        "SessionAssignmentActor-" + UUID.randomUUID(),
                        Props.empty()
                );

            // === 3. Create VenueCoordinatorActor ===
            ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> coordinatorActor = system.systemActorOf(
                    VenueCoordinatorActor.create(venues, venueActors),
                    "CoordinatorActor-" + UUID.randomUUID(),
                    Props.empty()
            );

            // === 4. Send test message to coordinator ===
            System.out.println("Sending assignment request to coordinator...");
            coordinatorActor.tell(new VenueCoordinatorActor.RequestVenueAssignment(
                    1,       // duration in hours
                    50,      // minimum capacity
                    sessionAssignmentActor  // replyTo
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
