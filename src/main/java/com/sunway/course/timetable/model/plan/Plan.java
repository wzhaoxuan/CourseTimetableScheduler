package com.sunway.course.timetable.model.plan;
import com.sunway.course.timetable.model.Satisfaction;
import com.sunway.course.timetable.model.plancontent.PlanContent;
import com.sunway.course.timetable.model.plancontent.PlanContentId;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "plan")
public class Plan {

    @EmbeddedId
    private PlanContentId planId;  // Using the same composite key as PlanContent

    @OneToOne
    @MapsId  // Automatically maps moduleId and sessionId from PlanContentId
    @JoinColumns({
        @JoinColumn(name = "module_id", referencedColumnName = "module_id"),
        @JoinColumn(name = "session_id", referencedColumnName = "session_id")
    })
    private PlanContent planContent;

    @ManyToOne(optional = false)
    @JoinColumn(name = "satisfaction_id", nullable = false)
    private Satisfaction satisfaction;

    public Plan() {}

    public Plan(PlanContentId planId, PlanContent planContent, Satisfaction satisfaction) {
        this.planId = planId;
        this.planContent = planContent;
        this.satisfaction = satisfaction;
    }

    public PlanContentId getPlanId() {
        return planId;
    }

    public void setPlanId(PlanContentId planId) {
        this.planId = planId;
    }

    public PlanContent getPlanContent() {
        return planContent;
    }

    public void setPlanContent(PlanContent planContent) {
        this.planContent = planContent;
    }

    public Satisfaction getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(Satisfaction satisfaction) {
        this.satisfaction = satisfaction;
    }
}

