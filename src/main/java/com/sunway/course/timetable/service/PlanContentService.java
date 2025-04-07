package com.sunway.course.timetable.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.plancontent.PlanContent;
import com.sunway.course.timetable.model.plancontent.PlanContentId;
import com.sunway.course.timetable.repository.PlanContentRepository;

@Service
public class PlanContentService {

    private final PlanContentRepository planContentRepository;

    @Autowired
    public PlanContentService(PlanContentRepository planContentRepository) {
        this.planContentRepository = planContentRepository;
    }

    public List<PlanContent> getAllPlanContents() {
        return planContentRepository.findAll();
    }

    public PlanContent getPlanContentById(PlanContentId id) {
        return planContentRepository.findById(id).orElse(null);
    }

    public PlanContent savePlanContent(PlanContent planContent) {
        return planContentRepository.save(planContent);
    }

    public void deletePlanContent(PlanContentId id) {
        planContentRepository.deleteById(id);
    }
}
