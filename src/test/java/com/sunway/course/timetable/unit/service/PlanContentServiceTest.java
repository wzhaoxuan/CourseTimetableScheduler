package com.sunway.course.timetable.unit.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.model.plancontent.PlanContent;
import com.sunway.course.timetable.model.plancontent.PlanContentId;
import com.sunway.course.timetable.repository.PlanContentRepository;
import com.sunway.course.timetable.service.PlanContentServiceImpl;

@ExtendWith(MockitoExtension.class)
public class PlanContentServiceTest {
    
    @Mock private PlanContentRepository planContentRepository;

    @InjectMocks private PlanContentServiceImpl planContentService;

    @Test
    @DisplayName("Test getAllPlanContents - Success")
    void testGetAllPlanContents() {
        List<PlanContent> mockList = List.of(new PlanContent(new PlanContentId(1L, 101L, 201L)));
        when(planContentRepository.findAll()).thenReturn(mockList);

        List<PlanContent> result = planContentService.getAllPlanContents();
        assertEquals(1, result.size());
        verify(planContentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test getPlanContentById - Success")
    void testGetPlanContentById() {
        PlanContentId id = new PlanContentId(1L, 101L, 201L);
        PlanContent planContent = new PlanContent(id);

        when(planContentRepository.findById(id)).thenReturn(Optional.of(planContent));

        Optional<PlanContent> result = planContentService.getPlanContentById(id);
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getPlanContentId());
        verify(planContentRepository).findById(id);
    }

    @Test
    @DisplayName("Test getPlanContentById - Not Found")
    void testGetPlanContentByIdNotFound() {
        PlanContentId id = new PlanContentId(2L, 102L, 202L);

        when(planContentRepository.findById(id)).thenReturn(Optional.empty());

        Optional<PlanContent> result = planContentService.getPlanContentById(id);
        assertTrue(result.isEmpty());
        verify(planContentRepository).findById(id);
    }

    @Test
    @DisplayName("Test savePlanContent - Success")
    void testSavePlanContent() {
        PlanContentId id = new PlanContentId(1L, 101L, 201L);
        PlanContent planContent = new PlanContent(id);

        when(planContentRepository.save(planContent)).thenReturn(planContent);

        PlanContent saved = planContentService.savePlanContent(planContent);
        assertEquals(id, saved.getPlanContentId());
        verify(planContentRepository).save(planContent);
    }

    @Test
    @DisplayName("Test savePlanContent - Repository Failure")
    void testSavePlanContentFailure() {
        PlanContentId id = new PlanContentId(3L, 103L, 203L);
        PlanContent planContent = new PlanContent(id);

        when(planContentRepository.save(planContent))
            .thenThrow(new RuntimeException("DB save failed"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            planContentService.savePlanContent(planContent);
        });

        assertEquals("DB save failed", exception.getMessage());
        verify(planContentRepository).save(planContent);
    }


    @Test
    @DisplayName("Test deletePlanContent - Success")
    void testDeletePlanContent() {
        PlanContentId id = new PlanContentId(1L, 101L, 201L);

        doNothing().when(planContentRepository).deleteById(id);
        planContentService.deletePlanContent(id);

        verify(planContentRepository).deleteById(id);
    }

    @Test
    @DisplayName("Test deletePlanContent - Repository Failure")
    void testDeletePlanContentFailure() {
        PlanContentId id = new PlanContentId(4L, 104L, 204L);

        doThrow(new RuntimeException("Delete error")).when(planContentRepository).deleteById(id);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            planContentService.deletePlanContent(id);
        });

        assertEquals("Delete error", exception.getMessage());
        verify(planContentRepository).deleteById(id);
    }
}
