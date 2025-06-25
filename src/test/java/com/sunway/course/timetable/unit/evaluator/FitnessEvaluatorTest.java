package com.sunway.course.timetable.unit.evaluator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.evaluator.FitnessEvaluator;
import com.sunway.course.timetable.evaluator.FitnessResult;
import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.model.Satisfaction;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.service.SatisfactionServiceImpl;
import com.sunway.course.timetable.service.venue.VenueDistanceServiceImpl;

@ExtendWith(MockitoExtension.class)
public class FitnessEvaluatorTest {

    @Mock
    private SatisfactionServiceImpl satisfactionService;

    @Mock
    private VenueDistanceServiceImpl venueDistanceService;

    private FitnessEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new FitnessEvaluator(satisfactionService, venueDistanceService);
    }

    @Test
    void testEvaluateEmptySchedule() {
        // 1) Evaluate with no sessions
        List<Session> sessions = List.of();
        Map<Session, Venue> sessionVenueMap = Map.of();
        String versionTag = "v1";

        FitnessResult result = evaluator.evaluate(sessions, sessionVenueMap, versionTag);

        // 2) Check computed fitness values
        assertEquals(100.0, result.getPercentage());
        assertEquals(0.0, result.getTotalPenalty());
        assertEquals(0.0, result.getMaxPenalty());

        // 3) All constraint checkers should appear (hard + soft)
        int totalViolations = result.getHardViolations().size() + result.getSoftViolations().size();
        // There are 13 checkers in the default list
        assertEquals(13, totalViolations);

        // 4) CURRENT_SESSION_KEYS should be empty
        assertTrue(FitnessEvaluator.CURRENT_SESSION_KEYS.isEmpty());

        // 5) Verify Satisfaction was saved correctly
        ArgumentCaptor<Satisfaction> cap = ArgumentCaptor.forClass(Satisfaction.class);
        verify(satisfactionService).saveSatisfaction(cap.capture());
        Satisfaction saved = cap.getValue();

        assertEquals(100.0, saved.getScore());
        assertEquals(0, saved.getConflict());
        assertEquals(versionTag, saved.getVersionTag());
        // SHA-256("") Base64 is well-known:
        assertEquals("47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=",
                     saved.getScheduleHash());
    }

    @Test
    @DisplayName("evaluate() on a tiny sample timetable yields a non-perfect fitness and correct keys")
    void testEvaluateSampleTimetable() {
        // --- 1) Create one student and one lecturer ---
        Student student = new Student(); student.setId(1L);
        Lecturer lecturer = new Lecturer(); lecturer.setId(10L);

        // --- 2) Build two Session objects on Monday 8:00–9:00 and 9:00–10:00 ---
        Session s1 = new Session();
        s1.setStudent(student);
        s1.setLecturer(lecturer);
        s1.setDay("MONDAY");
        s1.setStartTime(LocalTime.of(8,0));
        s1.setEndTime(LocalTime.of(9,0));
        s1.setType("Lecture");
        s1.setTypeGroup("M1-Lecture-G1");

        Session s2 = new Session();
        s2.setStudent(student);
        s2.setLecturer(lecturer);
        s2.setDay("MONDAY");
        s2.setStartTime(LocalTime.of(9,0));
        s2.setEndTime(LocalTime.of(10,0));
        s2.setType("Practical");
        s2.setTypeGroup("M1-Practical-G1");

        List<Session> sessions = List.of(s1, s2);

        // --- 3) Create two venues and map sessions to them ---
        Venue vA = new Venue(100L, "Room", "A1", 30, "Campus", "L1");
        Venue vB = new Venue(101L, "Room", "B1", 30, "Campus", "L1");

        Map<Session,Venue> map = Map.of(
        s1, vA,
        s2, vB
        );

        // --- 4) Stub distance service to introduce a penalty ---
        when(venueDistanceService.getDistanceScore(vA.getName(), vB.getName())).thenReturn(5.0);

        // --- 5) Evaluate ---
        FitnessResult result = evaluator.evaluate(sessions, map, "v2");

        // --- 6) Assertions ---

        // (a) Because of the distance penalty, fitness% should be < 100
        assertTrue(result.getPercentage() < 100.0, 
        "Distance penalty should lower fitness below 100%");

        // (b) CURRENT_SESSION_KEYS must contain the two session‐strings
        assertEquals(2, FitnessEvaluator.CURRENT_SESSION_KEYS.size());
        assertTrue(FitnessEvaluator.CURRENT_SESSION_KEYS.contains(
        "MONDAY-08:00-M1-Lecture-G1-A1"
        ));
        assertTrue(FitnessEvaluator.CURRENT_SESSION_KEYS.contains(
        "MONDAY-09:00-M1-Practical-G1-B1"
        ));

        // (c) Check that we saved a Satisfaction with the matching hash
        ArgumentCaptor<Satisfaction> cap = ArgumentCaptor.forClass(Satisfaction.class);
        verify(satisfactionService).saveSatisfaction(cap.capture());
        String savedHash = cap.getValue().getScheduleHash();

        // Recompute the expected hash from the same two strings, sorted:
        List<String> sorted = List.of(
        "MONDAY-08:00-M1-Lecture-G1-A1",
        "MONDAY-09:00-M1-Practical-G1-B1"
        );
        String combined = String.join("|", sorted);
        String expectedHash;
        try {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        expectedHash = Base64.getEncoder()
                .encodeToString(md.digest(combined.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
        throw new RuntimeException(e);
        }
        assertEquals(expectedHash, savedHash, "Schedule hash should match expected");
    }

}

