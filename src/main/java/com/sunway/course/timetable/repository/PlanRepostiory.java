package com.sunway.course.timetable.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunway.course.timetable.model.plan.Plan;
import com.sunway.course.timetable.model.plan.PlanId;

@Repository
public interface PlanRepostiory extends JpaRepository<Plan, PlanId> {
    // Custom query methods can be defined here if needed


}
