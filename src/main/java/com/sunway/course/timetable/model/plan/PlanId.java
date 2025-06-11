package com.sunway.course.timetable.model.plan;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PlanId {

    @Column(name = "id")
    private Long id;

    @Column(name = "plan_content_id")
    private Long planContentId;

    public PlanId() {
        // Default constructor
    }

    public PlanId(Long id, Long planContentId) {
        this.id = id;
        this.planContentId = planContentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlanContentId() {
        return planContentId;
    }

    public void setPlanContentId(Long planContentId) {
        this.planContentId = planContentId;
    }

    

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanId)) return false;
        PlanId that = (PlanId) o;
        return id.equals(that.id) && planContentId.equals(that.planContentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, planContentId);
    }
}
