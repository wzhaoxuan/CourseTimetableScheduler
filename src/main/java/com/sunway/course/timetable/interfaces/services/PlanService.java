package com.sunway.course.timetable.interfaces.services;
import java.util.List;
import java.util.Optional;

import com.sunway.course.timetable.model.plan.Plan;
import com.sunway.course.timetable.model.plancontent.PlanContentId;

public interface PlanService {
    Plan savePlan(Plan plan);
    Optional<Plan> getPlanById(PlanContentId planContentId);
    List<Plan> getAllPlans();
    void deletePlan(PlanContentId planContentId);
}
