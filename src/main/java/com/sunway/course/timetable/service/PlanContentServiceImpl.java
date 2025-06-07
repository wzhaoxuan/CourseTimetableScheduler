package com.sunway.course.timetable.service;
import java.util.Optional;
import java.util.List;

import com.sunway.course.timetable.model.Session;
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
    public Optional<PlanContent> getPlanContentById(PlanContentId id) {
        return planContentRepository.findById(id);
    }

    @Override
    public PlanContent savePlanContent(PlanContent planContent) {
        return planContentRepository.findByModuleAndSession(planContent.getModule(), planContent.getSession())
            .map(existingPlanContent -> {
                // Update fields of existing plan content if needed
                existingPlanContent.setModule(planContent.getModule());
                existingPlanContent.setSession(planContent.getSession());
                // Add more fields to update as needed

                return planContentRepository.save(existingPlanContent);
            })
            .orElseGet(() -> planContentRepository.save(planContent)); // Insert new if not found
    }

    @Override
    public void deletePlanContent(PlanContentId id) {
        planContentRepository.deleteById(id);
    }

    public Optional<PlanContent> getModuleBySession(Session session) {
         return planContentRepository.findBySession(session);
    }
}
