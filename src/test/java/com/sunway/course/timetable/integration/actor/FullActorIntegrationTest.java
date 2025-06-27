package com.sunway.course.timetable.integration.actor;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import com.sunway.course.timetable.akka.actor.SessionAssignmentActor.SessionAssigned;
import com.sunway.course.timetable.akka.actor.SessionAssignmentActor.SessionAssignmentCommand;
import com.sunway.course.timetable.akka.actor.SessionAssignmentActor.SessionAssignmentFailed;
import com.sunway.course.timetable.akka.actor.VenueActor;
import com.sunway.course.timetable.akka.actor.VenueCoordinatorActor;
import com.sunway.course.timetable.engine.DomainPruner.AssignmentOption;
import com.sunway.course.timetable.engine.DomainRejectionReason;
import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.service.venue.VenueDistanceServiceImpl;
import com.sunway.course.timetable.service.venue.VenueServiceImpl;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
import com.sunway.course.timetable.singleton.StudentAvailabilityMatrix;
import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;


/**
 * Automated/Full integration test for VenueCoordinatorActor and VenueActor.
 * 
 * This test simulates the entire flow of requesting a venue assignment
 * and verifies that the actors communicate correctly and assign venues as expected.
 * 
 * It uses the ActorTestKit to create a test environment
 * and TestProbe to capture messages sent between actors.
 * 
 * This test assumes that the VenueActor and VenueCoordinatorActor
 * are implemented correctly and that the VenueAvailabilityMatrix and
 * LecturerAvailabilityMatrix are functioning as expected.
 * 
 */
public class FullActorIntegrationTest {

    private static ActorTestKit testKit;

    private VenueAvailabilityMatrix venueMatrix;
    private LecturerAvailabilityMatrix lecturerMatrix;
    private StudentAvailabilityMatrix studentMatrix;
    private VenueDistanceServiceImpl venueDistanceService;
    private VenueServiceImpl venueService;

    @BeforeAll
    static void initAll() {
        testKit = ActorTestKit.create();
    }

    @AfterAll
    static void tearDownAll() {
        testKit.shutdownTestKit();
    }

    @BeforeEach
    void setup() {
        venueMatrix = new VenueAvailabilityMatrix(List.of());
        venueMatrix.initialize();

        lecturerMatrix = new LecturerAvailabilityMatrix();
        studentMatrix = new StudentAvailabilityMatrix();

        venueDistanceService = mock(VenueDistanceServiceImpl.class);
        venueService = mock(VenueServiceImpl.class);
    }

    @Test
    public void testSuccessfulSessionAssignmentFlow() {
        // --- 1) Prepare a single Venue and spawn its actor ---
        Venue venue = new Venue(8L, "Room", "UW2-1", 234, "Uni West", "Level 1");

        venueMatrix = new VenueAvailabilityMatrix(List.of(venue));
        venueMatrix.initialize();

        ActorRef<VenueActor.VenueCommand> venueActor =
            testKit.spawn(
                VenueActor.create(venue, venueMatrix, lecturerMatrix),
                "VenueActor-" + UUID.randomUUID()
            );

        // --- 2) Prepare a TestProbe for SessionAssignmentActor.Protocol ---
        TestProbe<SessionAssignmentCommand> probe = testKit.createTestProbe();

        // --- 3) Register lecturer and initialize students ---
        String lecturerId = "lec001";
        lecturerMatrix.registerLecturer(lecturerId);

        Student s1 = new Student();
        s1.setId(100L);
        studentMatrix.initializeStudents(List.of(s1));

        // --- 4) Spawn VenueCoordinatorActor with correct parameters ---
        ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> coordinator =
            testKit.spawn(
                VenueCoordinatorActor.create(
                    List.of(venue),
                    List.of(venueActor),
                    lecturerMatrix,
                    venueMatrix,
                    studentMatrix,
                    venueDistanceService,
                    venueService
                ),
                "Coordinator-" + UUID.randomUUID()
            );

        // --- 5) Build dummy module and candidates list ---
        Module module = new Module();
        module.setId("TEST101");
        module.setName("Test Module");
        List<Student> candidates = List.of(s1);

        //  1. Build a single domain option: day 0 (Monday), slot 0 (8:00am), our test venue
        List<AssignmentOption> prunedDomain = List.of(
            new AssignmentOption(0, 0, venue)
        );

        //  2. No particular “preferredVenues” is needed if you only have one option:
        List<String> preferred = Collections.emptyList();

        //  3. Still no rejections:
        List<DomainRejectionReason> rejections = Collections.emptyList();

        // --- 6) Send the RequestVenueAssignment message ---
        coordinator.tell(new VenueCoordinatorActor.RequestVenueAssignment(
            /* durationHours   */ 2,
            /* minCapacity     */ 50,
            /* lecturerName    */ lecturerId,
            /* module          */ module,
            /* eligibleStudents*/ candidates,
            /* sessionType     */ "Practical",
            /* groupIndex      */ 0,
            /* groupCount      */ 1,
            /* replyTo         */ probe.getRef(),
            /* preferredVenues */ preferred,
            /* prunedDomain    */ prunedDomain,
            /* rejectionLogs   */ rejections
        ));
        // --- 7) Await and assert the response ---
        SessionAssignmentCommand msg = probe.receiveMessage();

        if (msg instanceof SessionAssigned assigned) {
            assertTrue(msg instanceof SessionAssigned, "Expected a SessionAssigned reply");

            assertEquals("UW2-1", assigned.venue.getName());
            assertEquals(2 * 2, assigned.durationSlots); // 2h → 4 slots
            assertTrue(assigned.dayIndex >= 0 && assigned.dayIndex < 5);

            // --- 8) Verify matrices updated correctly ---
            // Lecturer
            assertFalse(
                lecturerMatrix.isAvailable(
                    lecturerId,
                    assigned.dayIndex,
                    assigned.startIndex,
                    assigned.startIndex + assigned.durationSlots
                ),
                "Lecturer should now be marked busy"
            );
            // Venue
            assertFalse(
                venueMatrix.isAvailable(
                    assigned.venue,
                    assigned.startIndex,
                    assigned.startIndex + assigned.durationSlots,
                    assigned.dayIndex
                ),
                "Venue should now be marked occupied"
            );
        } else if (msg instanceof SessionAssignmentFailed failed) {
            fail("Assignment failed: " + failed.reason);
        } else {
            fail("Unexpected reply type: " + msg.getClass().getSimpleName());
        }
    }
}