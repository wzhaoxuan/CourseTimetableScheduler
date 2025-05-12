package com.sunway.course.timetable.edge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.repository.SessionRepository;
import com.sunway.course.timetable.service.SessionServiceImpl;

@ExtendWith(MockitoExtension.class)
public class SessionEdgeCaseTest {

    @Mock private SessionRepository sessionRepository;
    @InjectMocks private SessionServiceImpl sessionService;

    private Session session, session2;

    @BeforeEach
    void setUp() {
        session = new Session();
        session.setId(1L);
        session.setStartTime(null);
        session.setEndTime(null);
        session.setDay(null);

        session2 = new Session();
        session2.setId(2L);
        session2.setStartTime(null);
        session2.setEndTime(null);
        session2.setDay(null);
    }

    @Test
    @DisplayName("Test Save Session with Null Values")
    void testSaveSessionWithNullValues() {
        
        doThrow(new IllegalArgumentException("Session cannot be null"))
            .when(sessionRepository).save(session);

        // Then: verify that the exception is thrown
        assertThrows(IllegalArgumentException.class, () -> {
            sessionRepository.save(session);
        });

        // Verify that the session was saved
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Test Save Non-Existent Session")
    void testSaveNonExistentSession() {
        doThrow(new IllegalArgumentException("Session not found")).when(sessionRepository).save(session2);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sessionService.saveSession(session2);
        });

        assertEquals("Session not found", exception.getMessage());
        verify(sessionRepository).save(session2); 
    }


    @Test
    @DisplayName("Test Delete Non-Existent Session")
    void testDeleteNonExistentSession() {
        doThrow(new IllegalArgumentException("Session not found")).when(sessionRepository).deleteById(1L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sessionService.deleteSession(1L);
        });

        assertEquals("Session not found", exception.getMessage());
        verify(sessionRepository).deleteById(1L); 
    }

    @Test
    @DisplayName("Test Delete Session with Invalid Id")
    void testDeleteSessionWithInvalidId() {

        doThrow(new IllegalArgumentException("Delete an invalid ID"))
            .when(sessionRepository).deleteById(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            sessionService.deleteSession(null);  // Assuming the service throws for null IDs
        });

        assertEquals("Delete an invalid ID", exception.getMessage());
    }

}
