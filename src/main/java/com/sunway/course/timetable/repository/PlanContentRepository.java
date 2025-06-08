package com.sunway.course.timetable.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.plancontent.PlanContent;
import com.sunway.course.timetable.model.plancontent.PlanContentId;


@Repository
public interface PlanContentRepository extends JpaRepository<PlanContent, PlanContentId> {
    // Custom query methods can be defined here if needed
    // For example, findByName(String name) to find students by their name

    Optional<PlanContent> findByModuleAndSession(Module module, Session session);
    Optional<PlanContent> findBySessionId(Long sessionId);
}
