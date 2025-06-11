package com.sunway.course.timetable.model.plan;
import java.io.Serializable;
import java.util.Objects;

import com.sunway.course.timetable.model.plancontent.PlanContentId;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class PlanId implements Serializable {

    @Embedded
    private PlanContentId planContentId;

    @Column(name = "satisfaction_id")
    private Long satisfactionId;

    public PlanId() {}

    public PlanId(PlanContentId planContentId, Long satisfactionId) {
        this.planContentId = planContentId;
        this.satisfactionId = satisfactionId;
    }

    public PlanContentId getPlanContentId() {
        return planContentId;
    }

    public void setPlanContentId(PlanContentId planContentId) {
        this.planContentId = planContentId;
    }

    public Long getSatisfactionId() {
        return satisfactionId;
    }

    public void setSatisfactionId(Long satisfactionId) {
        this.satisfactionId = satisfactionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanId)) return false;
        PlanId that = (PlanId) o;
        return Objects.equals(planContentId, that.planContentId) &&
               Objects.equals(satisfactionId, that.satisfactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planContentId, satisfactionId);
    }
}

