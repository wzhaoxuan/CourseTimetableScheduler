package com.sunway.course.timetable.repository;
import java.util.Optional;
import com.sunway.course.timetable.model.plan.Plan;
import com.sunway.course.timetable.model.plan.PlanId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepostiory extends JpaRepository<Plan, PlanId> {
    // Custom query methods can be defined here if needed
    // For example, findByName(String name) to find plans by their name

    Optional<Plan> findByName(String name);


}
