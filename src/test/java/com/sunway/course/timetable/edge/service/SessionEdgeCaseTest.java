package com.sunway.course.timetable.edge.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.repository.SessionRepository;
import com.sunway.course.timetable.service.SessionServiceImpl;

@ExtendWith(MockitoExtension.class)
public class SessionEdgeCaseTest {

    @Mock private SessionRepository sessionRepository;
    @InjectMocks private SessionServiceImpl sessionService;

    private Session incompleteSession;

    @BeforeEach
    void setUp() {
        incompleteSession = new Session();
        incompleteSession.setId(1L);
        incompleteSession.setType(null);
        incompleteSession.setTypeGroup(null);
        incompleteSession.setStartTime(null);
        incompleteSession.setEndTime(null);
        incompleteSession.setDay(null);
        incompleteSession.setLecturer(new Lecturer());
        incompleteSession.setStudent(new Student());
    }

    @Test
    @DisplayName("Test saveSession throws when session is null")
    void testSaveNullSession() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            sessionService.saveSession(null);
        });
        assertNotNull(exception);
    }

    @Test
    @DisplayName("Test saveSession throws when lecturer/student is null")
    void testSaveSessionMissingLecturerOrStudent() {
        Session invalid = new Session();
        invalid.setType("Lecture");
        invalid.setTypeGroup("G1");

        Exception exception = assertThrows(NullPointerException.class, () -> {
            sessionService.saveSession(invalid);
        });

        assertNotNull(exception);
    }

    @Test
    @DisplayName("Test deleteSession with null ID throws")
    void testDeleteNullId() {
        doThrow(new IllegalArgumentException("Delete an invalid ID"))
            .when(sessionRepository).deleteById(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            sessionService.deleteSession(null);
        });

        assertEquals("Delete an invalid ID", ex.getMessage());
    }

    @Test
    @DisplayName("Test deleteSession throws for non-existent ID")
    void testDeleteNonexistentId() {
        doThrow(new IllegalArgumentException("Session not found"))
            .when(sessionRepository).deleteById(99L);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            sessionService.deleteSession(99L);
        });

        assertEquals("Session not found", ex.getMessage());
    }
}

