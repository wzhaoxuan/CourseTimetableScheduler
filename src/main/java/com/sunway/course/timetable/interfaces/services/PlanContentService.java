package com.sunway.course.timetable.interfaces.services;

import java.util.List;

import com.sunway.course.timetable.model.plancontent.PlanContent;
import com.sunway.course.timetable.model.plancontent.PlanContentId;

public interface PlanContentService {
    List<PlanContent> getAllPlanContents();
    PlanContent getPlanContentById(PlanContentId id);
    PlanContent savePlanContent(PlanContent planContent);
    void deletePlanContent(PlanContentId id);

}
