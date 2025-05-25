package com.sunway.course.timetable.engine;
import java.util.List;
import java.util.Objects;

import com.sunway.course.timetable.model.Session;

public class Variable {
    private Session session;
    private List<TimeSlot> domain;
    private String id;

    public Variable(Session session, List<TimeSlot> domain) {
        this.session = session;
        this.domain = domain;
        this.id = session.getId() != null ? session.getId().toString() : session.getTempId();
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public List<TimeSlot> getDomain() {
        return domain;
    }

    public void setDomain(List<TimeSlot> domain) {
        this.domain = domain;
    }

    public Long getLecturerId() {
        return session.getLecturer() != null ? session.getLecturer().getId() : null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Variable other = (Variable) obj;

        String thisId = session.getId() != null ? session.getId().toString() : session.getTempId();
        String otherId = other.session.getId() != null ? other.session.getId().toString() : other.session.getTempId();

        return Objects.equals(thisId, otherId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Variable{" +
                "session=" + session +
                ", domain=" + domain +
                ", id='" + id + '\'' +
                '}';
    }
}
