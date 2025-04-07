package com.sunway.course.timetable.model.plancontent;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PlanContentId {

    @Column(name = "id")
    private Long id;

    @Column(name = "module_id")
    private Long moduleId;

    @Column(name = "session_id")
    private Long sessionId;

    public PlanContentId() {
        // Default constructor
    }

    public PlanContentId(Long id, Long moduleId, Long sessionId) {
        this.id = id;
        this.moduleId = moduleId;
        this.sessionId = sessionId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanContentId)) return false;
        PlanContentId that = (PlanContentId) o;
        return id.equals(that.id) && moduleId.equals(that.moduleId) && sessionId.equals(that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, moduleId, sessionId);
    }

}
