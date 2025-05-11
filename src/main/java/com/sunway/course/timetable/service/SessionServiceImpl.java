package com.sunway.course.timetable.service;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.repository.SessionRepository;
import com.sunway.course.timetable.interfaces.services.SessionService;

import java.util.List;

import org.springframework.stereotype.Service;

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
        return sessionRepository.save(session);
    }

    @Override
    public void deleteSession(Long id) {
        sessionRepository.deleteById(id);
    }

}
