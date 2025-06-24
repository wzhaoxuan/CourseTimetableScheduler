package com.sunway.course.timetable.unit.service;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.repository.SessionRepository;
import com.sunway.course.timetable.service.SessionServiceImpl;

@ExtendWith(MockitoExtension.class)
public class SessionServiceImplTest {

    @Mock private SessionRepository sessionRepository;
    @InjectMocks private SessionServiceImpl sessionService;

    @Test
    void testGetAllSessions() {
        List<Session> sessions = List.of(mock(Session.class), mock(Session.class));
        when(sessionRepository.findAll()).thenReturn(sessions);

        List<Session> result = sessionService.getAllSessions();
        assertEquals(2, result.size());
        verify(sessionRepository).findAll();
    }

    @Test
    void testGetSessionById_found() {
        Session session = mock(Session.class);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        Session result = sessionService.getSessionById(1L);
        assertEquals(session, result);
    }

    @Test
    void testGetSessionById_notFound() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());
        assertNull(sessionService.getSessionById(1L));
    }

    @Test
    void testSaveSession_existingSessionUpdated() {
        // Setup input session
        Session input = new Session();
        input.setType("Practical");
        input.setTypeGroup("G1");
        Lecturer lecturer = new Lecturer(); lecturer.setId(1L);
        Student student = new Student(); student.setId(2L);
        input.setLecturer(lecturer);
        input.setStudent(student);
        input.setDay("Monday");

        // Existing session to be updated
        Session existing = new Session();
        existing.setId(100L);

        when(sessionRepository.findByTypeAndTypeGroupAndLecturerIdAndStudentId(
                "Practical", "G1", 1L, 2L))
            .thenReturn(Optional.of(existing));

        when(sessionRepository.save(existing)).thenReturn(existing);

        Session result = sessionService.saveSession(input);
        assertEquals(existing, result);
        verify(sessionRepository).save(existing);
    }

    @Test
    void testSaveSession_newSessionInserted() {
        Session input = new Session();
        Lecturer lecturer = new Lecturer(); lecturer.setId(1L);
        Student student = new Student(); student.setId(2L);
        input.setLecturer(lecturer);
        input.setStudent(student);
        input.setType("Lecture");
        input.setTypeGroup("G2");

        when(sessionRepository.findByTypeAndTypeGroupAndLecturerIdAndStudentId(
                "Lecture", "G2", 1L, 2L))
            .thenReturn(Optional.empty());

        when(sessionRepository.save(input)).thenReturn(input);

        Session result = sessionService.saveSession(input);
        assertEquals(input, result);
        verify(sessionRepository).save(input);
    }

    @Test
    void testDeleteSession() {
        sessionService.deleteSession(123L);
        verify(sessionRepository).deleteById(123L);
    }
}

