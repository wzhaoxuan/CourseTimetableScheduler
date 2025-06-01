package com.sunway.course.timetable.model.plan;
import com.sunway.course.timetable.model.Satisfaction;
import com.sunway.course.timetable.model.plancontent.PlanContent;

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
    private PlanId planId;

    @OneToOne
    @JoinColumn(name = "satisfaction_id", referencedColumnName = "id", unique = true)
    private Satisfaction satisfaction;

    @ManyToOne
    @MapsId("plancontentId")  // Maps the plancontentId part of the composite key (Only if PlanContent uses @EmbeddedId that includes planId)
    @JoinColumns({
        @JoinColumn(name = "session_id", referencedColumnName = "session_id"),
        @JoinColumn(name = "module_id", referencedColumnName = "module_id")
    })
    private PlanContent planContent;

    public Plan() {
        // Default constructor
    }

    public Plan(PlanId planId, PlanContent planContent, Satisfaction satisfaction) {
        this.planId = planId;
        this.planContent = planContent;
        this.satisfaction = satisfaction;
    }

    public Satisfaction getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(Satisfaction satisfaction) {
        this.satisfaction = satisfaction;
    }

}
