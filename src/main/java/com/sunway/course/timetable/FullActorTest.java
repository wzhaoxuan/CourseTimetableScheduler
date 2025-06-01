// package com.sunway.course.timetable;

// import java.util.List;
// import java.util.UUID;

// import org.springframework.stereotype.Service;

// import com.sunway.course.timetable.akka.actor.SessionAssignmentActor.SessionAssigned;
// import com.sunway.course.timetable.akka.actor.SessionAssignmentActor.SessionAssignmentCommand;
// import com.sunway.course.timetable.akka.actor.SessionAssignmentActor.SessionAssignmentFailed;
// import com.sunway.course.timetable.akka.actor.VenueActor;
// import com.sunway.course.timetable.akka.actor.VenueCoordinatorActor;
// import com.sunway.course.timetable.model.Venue;
// import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
// import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;

// import akka.actor.typed.ActorRef;
// import akka.actor.typed.ActorSystem;
// import akka.actor.typed.Props;
// import akka.actor.typed.javadsl.Behaviors;
// import jakarta.annotation.PostConstruct;

// @Service
// public class FullActorTest {

//     private final ActorSystem<Void> system;
//     private final VenueAvailabilityMatrix venueAvailabilityMatrix;
//     private final LecturerAvailabilityMatrix lecturerAvailabilityMatrix; // Injected singleton
//     private volatile boolean testRun = false;

//     public FullActorTest(ActorSystem<Void> system, VenueAvailabilityMatrix venueAvailabilityMatrix, LecturerAvailabilityMatrix lecturerAvailabilityMatrix) {
//         this.system = system;
//         this.venueAvailabilityMatrix = venueAvailabilityMatrix;
//         this.lecturerAvailabilityMatrix = lecturerAvailabilityMatrix;
//     }

//     @PostConstruct
//     public void runTest() {
//         if (testRun) return;
//         testRun = true;

//         try {
//             // === 1. Create venues and venue actors ===
//             Venue venue = new Venue(8L, "Auditorium", "JC1", 234, "Level 1", "University");
//             ActorRef<VenueActor.VenueCommand> venueActor = system.systemActorOf(
//                     VenueActor.create(venue, venueAvailabilityMatrix, lecturerAvailabilityMatrix),
//                     "VenueActor-" + UUID.randomUUID(),
//                     Props.empty());

//             List<Venue> venues = List.of(venue);
//             List<ActorRef<VenueActor.VenueCommand>> venueActors = List.of(venueActor);

//             // === 2. Create SessionAssignmentActor that logs result ===
//             ActorRef<SessionAssignmentCommand> sessionAssignmentActor =
//                 system.systemActorOf(
//                         Behaviors.setup(ctx ->
//                                 Behaviors.receive(SessionAssignmentCommand.class)
//                                         .onMessage(SessionAssigned.class, msg -> {
//                                             System.out.printf("Session assigned to %s on day %d, start %d, duration %d%n",
//                                                     msg.venue.getName(), msg.dayIndex, msg.startIndex, msg.durationSlots);
//                                                 System.out.println("Lecturer availability after assignment:");
//                                                 lecturerAvailabilityMatrix.printAvailability("lec001");

//                                                 System.out.println("Venue availability after assignment:");
//                                                 venueAvailabilityMatrix.printAvailability();

//                                             return Behaviors.stopped();
//                                         })
//                                         .onMessage(SessionAssignmentFailed.class, msg -> {
//                                             System.out.printf("Session assignment failed: %s%n", msg.reason);

//                                             return Behaviors.stopped();
//                                         })
//                                         .build()
//                         ),
//                         "SessionAssignmentActor-" + UUID.randomUUID(),
//                         Props.empty()
//                 );

//             // === 3. Create VenueCoordinatorActor ===
//             ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> coordinatorActor = system.systemActorOf(
//                     VenueCoordinatorActor.create(venues, venueActors, lecturerAvailabilityMatrix),
//                     "CoordinatorActor-" + UUID.randomUUID(),
//                     Props.empty()
//             );

//             // === 4. Send test message to coordinator ===
//             String lecturerId = "lec001";
//             lecturerAvailabilityMatrix.registerLecturer(lecturerId);
//             System.out.println("Sending assignment request to coordinator...");
//             coordinatorActor.tell(new VenueCoordinatorActor.RequestVenueAssignment(
//                     2,       // duration in hours
//                     50,      // minimum capacity
//                     lecturerId,  // lecturer ID
//                     sessionAssignmentActor  // replyTo
//             ));
//         } catch (Exception e) {
//             e.printStackTrace();
//         }

//         // Wait a short moment (just for demo, use better async approach in production)
//             try {
//                 Thread.sleep(1500); // wait for response to print
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//             }
//             // Then terminate the system here, safely after message processing
//             system.terminate();
//     }
// }
