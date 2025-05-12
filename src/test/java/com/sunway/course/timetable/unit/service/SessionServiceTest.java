package com.sunway.course.timetable.unit.service;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.repository.SessionRepository;
import com.sunway.course.timetable.service.SessionServiceImpl;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    @Mock private SessionRepository sessionRepository;
    @InjectMocks private SessionServiceImpl sessionService;

    private Session session, session2;
    private LocalTime startTime, endTime;

    @BeforeEach
    void setUp() {
        startTime = LocalTime.of(12, 0);
        endTime = LocalTime.of(14, 0);
        
        session = new Session();
        session.setId(1L);
        session.setStartTime(startTime);
        session.setEndTime(endTime);
        session.setDay("Monday");

        session2 = new Session();
        session2.setId(2L);
        session2.setStartTime(startTime);
        session2.setEndTime(endTime);
        session2.setDay("Tuesday");
        
    }

    @Test
    @DisplayName("Test Get All Sessions")
    void testGetAllSessions() {
        List<Session> sessions = Arrays.asList(session, session2);
        
        when(sessionRepository.findAll()).thenReturn(sessions);
        
        List<Session> result = sessionService.getAllSessions();

        assertEquals(2, result.size());
        verify(sessionRepository).findAll();
    }

    @Test
    @DisplayName("Return empty list when no sessions found")
    void testGetAllSessionsEmpty() {
        when(sessionRepository.findAll()).thenReturn(List.of());

        List<Session> result = sessionService.getAllSessions();

        assertEquals(0, result.size());
        verify(sessionRepository).findAll();
    }

    @Test
    @DisplayName("Test Get Session By ID -- Success")
    void testGetSessionById() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        Session result = sessionService.getSessionById(1L);

        assertNotNull(result);
        assertEquals("Monday", result.getDay());
        verify(sessionRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return null if session not found by ID")
    void testGetSessionByIdNotFound() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        Session result = sessionService.getSessionById(1L);

        assertNull(result);
        verify(sessionRepository).findById(1L);
    }

    @Test
    @DisplayName("Test Save Session")
    void testSaveSession() {
        when(sessionRepository.save(session)).thenReturn(session);

        Session result = sessionService.saveSession(session);

        assertNotNull(result);
        assertEquals("Monday", result.getDay());
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Test Delete Session")
    void testDeleteSession() {
        doNothing().when(sessionRepository).deleteById(1L);

        sessionService.deleteSession(1L);

        verify(sessionRepository).deleteById(1L);
    }
}
