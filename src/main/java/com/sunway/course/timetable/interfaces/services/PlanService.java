package com.sunway.course.timetable.interfaces.services;
import java.util.List;
import java.util.Optional;

import com.sunway.course.timetable.model.plan.Plan;
import com.sunway.course.timetable.model.plan.PlanId;

public interface PlanService {
    Plan savePlan(Plan plan);
    Optional<Plan> getPlanById(PlanId PlanId);
    List<Plan> getAllPlans();
    void deletePlan(PlanId PlanId);
}
