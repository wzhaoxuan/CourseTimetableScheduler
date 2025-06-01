package com.sunway.course.timetable.model.plancontent;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PlanContentId {

    @Column(name = "module_id")
    private String moduleId;

    @Column(name = "session_id")
    private Long sessionId;

    public PlanContentId() {
        // Default constructor
    }

    public PlanContentId(String moduleId, Long sessionId) {
        this.moduleId = moduleId;
        this.sessionId = sessionId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
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
        return moduleId.equals(that.moduleId) && sessionId.equals(that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleId, sessionId);
    }

}
