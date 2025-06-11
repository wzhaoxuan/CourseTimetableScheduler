package com.sunway.course.timetable.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sunway.course.timetable.model.plan.Plan;
import com.sunway.course.timetable.model.plan.PlanId;

@Repository
public interface PlanRepository extends JpaRepository<Plan, PlanId> {
    // Custom query methods can be defined here if needed

    @Query("""
        SELECT p FROM Plan p
        WHERE LOWER(p.planContent.session.lecturer.name) = LOWER(:lecturerName)
    """)
    List<Plan> findPlansByLecturer(@Param("lecturerName") String lecturerName);

    @Query("""
        SELECT p FROM Plan p
        WHERE p.planContent.module.id = :moduleId
    """)
    List<Plan> findPlansByModule(@Param("moduleId") String moduleId);
}
