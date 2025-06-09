package com.sunway.course.timetable.unit.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.model.Satisfaction;
import com.sunway.course.timetable.repository.SatisfactionRepository;
import com.sunway.course.timetable.service.SatisfactionServiceImpl;

@ExtendWith(MockitoExtension.class)
public class StatisfactionServiceTest {

    @Mock private SatisfactionRepository satisfactionRepository;
    @InjectMocks private SatisfactionServiceImpl statisfactionService;

    private Satisfaction satisfaction, satisfaction2;

    @BeforeEach
    void setUp() {
        satisfaction = new Satisfaction();
        satisfaction.setId(1L);
        satisfaction.setScore(80.0);
        satisfaction.setConflict(3);

        satisfaction2 = new Satisfaction();
        satisfaction2.setId(2L);
        satisfaction2.setScore(90.0);
        satisfaction2.setConflict(2);
    }

    @Test
    @DisplayName("Test Get All Satisfactions")
    void testGetAllSatisfactions() {
        List<Satisfaction> satisfactions = Arrays.asList(satisfaction, satisfaction2);
        
        when(satisfactionRepository.findAll()).thenReturn(satisfactions);
        
        List<Satisfaction> result = statisfactionService.getAllSatisfactions();

        assertEquals(2, result.size());
        verify(satisfactionRepository).findAll();
    }

    @Test
    @DisplayName("Return empty list when no satisfactions found")
    void testGetAllSatisfactionsEmpty() {
        when(satisfactionRepository.findAll()).thenReturn(Arrays.asList());

        List<Satisfaction> result = statisfactionService.getAllSatisfactions();

        assertEquals(0, result.size());
        verify(satisfactionRepository).findAll();
    }

    @Test
    @DisplayName("Test Get Satisfaction By ID -- Success")
    void testGetSatisfactionById() {
        when(satisfactionRepository.findById(1L)).thenReturn(Optional.of(satisfaction));

        Satisfaction result = statisfactionService.getSatisfactionById(1L);

        assertNotNull(result);
        assertEquals(80.0, result.getScore());
        verify(satisfactionRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return null if satisfaction not found by ID")
    void testGetSatisfactionByIdNotFound() {
        when(satisfactionRepository.findById(1L)).thenReturn(Optional.empty());

        Satisfaction result = statisfactionService.getSatisfactionById(1L);

        assertNull(result);
        verify(satisfactionRepository).findById(1L);
    }

    @Test
    @DisplayName("Test Save Satisfaction")
    void testSaveSatisfaction() {
        when(satisfactionRepository.save(satisfaction)).thenReturn(satisfaction);

        Satisfaction result = statisfactionService.saveSatisfaction(satisfaction);

        assertNotNull(result);
        assertEquals(80.0, result.getScore());
        verify(satisfactionRepository).save(satisfaction);
    }

    @Test
    @DisplayName("Test Delete Satisfaction")
    void testDeleteSatisfaction() {
        doNothing().when(satisfactionRepository).deleteById(1L);

        statisfactionService.deleteSatisfaction(1L);

        verify(satisfactionRepository).deleteById(1L);
    }
}
