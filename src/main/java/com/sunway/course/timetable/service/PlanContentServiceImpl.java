package com.sunway.course.timetable.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.interfaces.services.PlanContentService;
import com.sunway.course.timetable.model.plancontent.PlanContent;
import com.sunway.course.timetable.model.plancontent.PlanContentId;
import com.sunway.course.timetable.repository.PlanContentRepository;

@Service
public class PlanContentServiceImpl implements PlanContentService{

    private final PlanContentRepository planContentRepository;

    public PlanContentServiceImpl(PlanContentRepository planContentRepository) {
        this.planContentRepository = planContentRepository;
    }

    @Override
    public List<PlanContent> getAllPlanContents() {
        return planContentRepository.findAll();
    }

    @Override
    public PlanContent getPlanContentById(PlanContentId id) {
        return planContentRepository.findById(id).orElse(null);
    }

    @Override
    public PlanContent savePlanContent(PlanContent planContent) {
        return planContentRepository.save(planContent);
    }

    @Override
    public void deletePlanContent(PlanContentId id) {
        planContentRepository.deleteById(id);
    }
}
