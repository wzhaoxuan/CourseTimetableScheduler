package com.sunway.course.timetable.interfaces.services;

import java.util.List;

import com.sunway.course.timetable.model.Session;

public interface SessionService {
    List<Session> getAllSessions();
    Session getSessionById(Long id);
    Session saveSession(Session session);
    void deleteSession(Long id);

}
