package com.sunway.course.timetable.service;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.interfaces.services.PlanService;
import com.sunway.course.timetable.model.plan.Plan;
import com.sunway.course.timetable.model.plancontent.PlanContentId;
import com.sunway.course.timetable.repository.PlanRepository;
import java.util.List;
import java.util.Optional;

@Service
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    public PlanServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public Plan savePlan(Plan plan) {
        if (plan == null) throw new IllegalArgumentException("Plan cannot be null");
        return planRepository.save(plan);
    }

    @Override
    public Optional<Plan> getPlanById(PlanContentId planContentId) {
        return planRepository.findById(planContentId);
    }

    @Override
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    @Override
    public void deletePlan(PlanContentId planContentId) {
        planRepository.deleteById(planContentId);
    }
}

