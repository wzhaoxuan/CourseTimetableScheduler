package com.sunway.course.timetable.model.plan;
import com.sunway.course.timetable.model.Satisfaction;
import com.sunway.course.timetable.model.plancontent.PlanContent;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "plan")
public class Plan {

    @EmbeddedId
    private PlanId planId;

    @ManyToOne
    @MapsId("planContentId")  // Tell JPA that planContentId inside embeddedId maps to planContent
    @JoinColumns({
        @JoinColumn(name = "module_id", referencedColumnName = "module_id"),
        @JoinColumn(name = "session_id", referencedColumnName = "session_id")
    })
    private PlanContent planContent;

    @ManyToOne(optional = false)
    @MapsId("satisfactionId")  // Tell JPA that satisfactionId inside embeddedId maps to satisfaction
    @JoinColumn(name = "satisfaction_id", referencedColumnName = "id")
    private Satisfaction satisfaction;

    public Plan() {}

    public Plan(PlanId planId, PlanContent planContent, Satisfaction satisfaction) {
        this.planId = planId;
        this.planContent = planContent;
        this.satisfaction = satisfaction;
    }

    public PlanId getPlanId() {
        return planId;
    }

    public void setPlanId(PlanId planId) {
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


