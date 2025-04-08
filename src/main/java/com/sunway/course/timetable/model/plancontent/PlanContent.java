package com.sunway.course.timetable.model.plancontent;
import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Session;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "plan_content")
public class PlanContent {

    @EmbeddedId
    private PlanContentId planContentId;

    @ManyToOne
    @MapsId("moduleId") // Maps moduleId from the embedded composite key
    @JoinColumn(name = "module_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Module module;

    @ManyToOne
    @MapsId("sessionId") // Maps sessionId from the embedded composite key
    @JoinColumn(name = "session_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Session session;

    public PlanContent() {
        // Default constructor
    }

    public PlanContent(PlanContentId planContentId) {
        this.planContentId = planContentId;
    }

    public PlanContentId getPlanContentId() {
        return planContentId;
    }

    public void setPlanContentId(PlanContentId planContentId) {
        this.planContentId = planContentId;
    }

}
