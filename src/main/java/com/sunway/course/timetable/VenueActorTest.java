package com.sunway.course.timetable;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.akka.actor.VenueActor;
import com.sunway.course.timetable.model.Venue;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import akka.actor.typed.javadsl.Behaviors;
import jakarta.annotation.PostConstruct;

@Service
public class VenueActorTest {

        // Create the ActorSystem
    private final ActorSystem<Void> system;
    private volatile boolean testRun = false; // guard to prevent multiple runs

    public VenueActorTest(ActorSystem<Void> system) {
        this.system = system;
    }

    @PostConstruct
    public void runTest() {
        if (testRun) return; // prevent multiple runs
        testRun = true;
        try{
            // Create a test venue
            Venue testVenue = new Venue("Auditorium", "JC1", 234, "Level 1", "University");

            // Spawn the VenueActor
            ActorRef<VenueActor.VenueCommand> venueActor =
                    system.systemActorOf(VenueActor.create(testVenue), "VenueActor" + UUID.randomUUID().toString().replace("-", ""), Props.empty());

            // Create a receiver actor for the response
            ActorRef<VenueActor.VenueResponse> responseReceiver =
                    system.systemActorOf(
                            Behaviors.receive(VenueActor.VenueResponse.class)
                                    .onMessage(VenueActor.VenueAccepted.class, msg -> {
                                        System.out.printf("Accepted: %s %d %d %d%n",
                                                msg.venue.getName(), msg.dayIndex,
                                                msg.startIndex, msg.durationSlots);
                                        return Behaviors.stopped();
                                    })
                                    .onMessage(VenueActor.VenueRejected.class, msg -> {
                                        System.out.printf("Rejected: %s %s%n",
                                                msg.venue.getName(), msg.reason);
                                        return Behaviors.stopped();
                                    })
                                    .build(),
                            "ResponseReceiver",
                            Props.empty()
                    );

            // Send the test message
            for (int day = 0; day < 5; day++) {
                for (int start = 0; start < 16; start += 4) { // test slots in chunks of 4
                    int end = start + 4;
                    System.out.printf("Requesting day=%d, start=%d, end=%d%n", day, start, end);
                    venueActor.tell(new VenueActor.CheckAndAssignSlot(day, start, end, responseReceiver));
                }
            }
            // Wait a short moment (just for demo, use better async approach in production)
            try {
                Thread.sleep(1500); // wait for response to print
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // Then terminate the system here, safely after message processing
            system.terminate();
        }
        catch (Exception e) {
            e.printStackTrace();
        } 
        
    }
    
}
