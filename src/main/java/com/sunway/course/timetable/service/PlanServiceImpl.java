package com.sunway.course.timetable.service;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.interfaces.services.PlanService;
import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;
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

    /**
     * Programme filter — requires moduleDataMap (preloaded from preprocessing step)
     */
    public List<Plan> getPlansByProgramme(String programmeCode, Map<String, ModuleAssignmentData> moduleDataMap) {
        return planRepository.findAll().stream()
            .filter(plan -> {
                String moduleId = plan.getPlanContent().getModule().getId();
                ModuleAssignmentData data = moduleDataMap.get(moduleId);
                if (data == null) return false;

                return data.getProgrammeOfferingModules().stream()
                        .anyMatch(prog -> prog.getProgrammeId().getId().equalsIgnoreCase(programmeCode));
            })
            .collect(Collectors.toList());
    }

    public List<Plan> getPlansByLecturer(String lecturerName) {
        return planRepository.findPlansByLecturer(lecturerName);
    }

    public List<Plan> getPlansByModule(String moduleId) {
        return planRepository.findPlansByModule(moduleId);
    }
}


