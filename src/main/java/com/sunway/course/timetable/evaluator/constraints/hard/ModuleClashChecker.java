package com.sunway.course.timetable.evaluator.constraints.hard;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sunway.course.timetable.evaluator.ConstraintChecker;
import com.sunway.course.timetable.evaluator.ConstraintType;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;

public class ModuleClashChecker implements ConstraintChecker {

    @Override
    public String getName() {
        return "Module Clashes";
    }

    @Override
    public ConstraintType getType() {
        return ConstraintType.HARD;
    }

    @Override
    public double getWeight() {
        return 500.0;
    }

    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        Map<Long, List<Session>> sessionsByStudent = new HashMap<>();

        for (Session s : sessions) {
            if (s.getStudent() == null) continue;
            sessionsByStudent.computeIfAbsent(s.getStudent().getId(), k -> new ArrayList<>()).add(s);
        }

        int clashCount = 0;

        for (List<Session> studentSessions : sessionsByStudent.values()) {
            studentSessions.sort(Comparator
                .comparing(Session::getDay)
                .thenComparing(Session::getStartTime));

            for (int i = 0; i < studentSessions.size(); i++) {
                Session a = studentSessions.get(i);
                String moduleA = extractModuleIdFromTypeGroup(a.getTypeGroup());
                String typeGroupA = a.getTypeGroup();

                for (int j = i + 1; j < studentSessions.size(); j++) {
                    Session b = studentSessions.get(j);
                    String moduleB = extractModuleIdFromTypeGroup(b.getTypeGroup());
                    String typeGroupB = b.getTypeGroup();

                    if (!a.getDay().equals(b.getDay())) break;
                    if (!moduleA.equals(moduleB)) continue;

                    // Same module but different type-group, check overlap
                    if (!typeGroupA.equals(typeGroupB) && overlaps(a.getStartTime(), a.getEndTime(), b.getStartTime(), b.getEndTime())) {
                        clashCount++;
                    }
                }
            }
        }

        return clashCount;
    }

    private String extractModuleIdFromTypeGroup(String typeGroup) {
        if (typeGroup == null || !typeGroup.contains("-")) return null;
        return typeGroup.split("-")[0];
    }

    private boolean overlaps(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
