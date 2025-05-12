package com.sunway.course.timetable.interfaces.services;
import java.util.List;
import java.util.Optional;

import com.sunway.course.timetable.model.plancontent.PlanContent;
import com.sunway.course.timetable.model.plancontent.PlanContentId;

public interface PlanContentService {
    List<PlanContent> getAllPlanContents();
    Optional<PlanContent> getPlanContentById(PlanContentId id);
    PlanContent savePlanContent(PlanContent planContent);
    void deletePlanContent(PlanContentId id);
}
