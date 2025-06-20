package com.sunway.course.timetable.service;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.interfaces.services.PlanService;
import com.sunway.course.timetable.model.plan.Plan;
import com.sunway.course.timetable.model.plan.PlanId;
import com.sunway.course.timetable.repository.PlanRepository;

@Service
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    public PlanServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public Plan savePlan(Plan plan) {
        // if (plan == null) throw new IllegalArgumentException("Plan cannot be null");
        return planRepository.save(plan);
    }

    @Override
    public Optional<Plan> getPlanById(PlanId planId) {
        return planRepository.findById(planId);
    }

    @Override
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    @Override
    public void deletePlan(PlanId planId) {
        planRepository.deleteById(planId);
    }

    public List<Plan> getPlansByLecturerAndVersion(String lecturerName, String versionTag) {
        return planRepository.findPlansByLecturerAndVersion(lecturerName, versionTag);
    }

    public List<Plan> getPlansByModuleAndVersion(String moduleId, String versionTag) {
        return planRepository.findPlansByModuleAndVersion(moduleId, versionTag);
    }

    public List<String> getAllVersionsByLecturer(String lecturerName) {
        return planRepository.findDistinctVersionsByLecturer(lecturerName);
    }

    public List<String> getAllVersionsByModule(String moduleId) {
        return planRepository.findDistinctVersionsByModule(moduleId);
    }

}


