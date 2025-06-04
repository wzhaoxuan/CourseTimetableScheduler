package com.sunway.course.timetable.service;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.interfaces.services.SessionService;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.repository.SessionRepository;

@Service
public class SessionServiceImpl implements SessionService {
    private final SessionRepository sessionRepository;

    public SessionServiceImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }
    
    @Override
    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    @Override
    public Session getSessionById(Long id) {
        return sessionRepository.findById(id).orElse(null);
    }

    @Override
    public Session saveSession(Session session) {
        return sessionRepository.findByTypeAndTypeGroupAndLecturerIdAndStudentId(
            session.getType(), session.getTypeGroup(), session.getLecturer().getId(), session.getStudent().getId())
            .map(existingSession -> {
                // Update fields of existing session if needed
                existingSession.setDay(session.getDay());
                existingSession.setStartTime(session.getStartTime());
                existingSession.setEndTime(session.getEndTime());
                existingSession.setType(session.getType());
                existingSession.setTypeGroup(session.getTypeGroup());
                // Add more fields to update as needed

                return sessionRepository.save(existingSession);
            })
            .orElseGet(() -> sessionRepository.save(session)); // Insert new if not found
        }

    @Override
    public void deleteSession(Long id) {
        sessionRepository.deleteById(id);
    }
}
