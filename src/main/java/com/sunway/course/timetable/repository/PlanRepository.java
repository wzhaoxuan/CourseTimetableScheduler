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
      AND p.satisfaction.versionTag = :versionTag
    """)
    List<Plan> findPlansByLecturerAndVersion(@Param("lecturerName") String lecturerName,
                                            @Param("versionTag") String versionTag);


    @Query("""
    SELECT p FROM Plan p
    WHERE p.planContent.module.id = :moduleId
      AND p.satisfaction.versionTag = :versionTag
    """)
    List<Plan> findPlansByModuleAndVersion(@Param("moduleId") String moduleId,
                                        @Param("versionTag") String versionTag);

    @Query("""
        SELECT DISTINCT p.satisfaction.versionTag FROM Plan p
        WHERE LOWER(p.planContent.session.lecturer.name) = LOWER(:lecturerName)
        ORDER BY p.satisfaction.versionTag
    """)
    List<String> findDistinctVersionsByLecturer(@Param("lecturerName") String lecturerName);

    @Query("""
    SELECT DISTINCT p.satisfaction.versionTag FROM Plan p
    WHERE p.planContent.module.id = :moduleId
    """)
    List<String> findDistinctVersionsByModule(@Param("moduleId") String moduleId);


}
