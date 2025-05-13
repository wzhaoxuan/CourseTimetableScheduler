package com.sunway.course.timetable.unit.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.model.WeekDayConstraint;
import com.sunway.course.timetable.repository.WeekDayConstraintRepository;
import com.sunway.course.timetable.service.WeekDayConstraintServiceImpl;
import com.sunway.course.timetable.exception.ValueNotFoundException;


@ExtendWith(MockitoExtension.class)
public class WeekDayConstraintServiceTest {

    @Mock WeekDayConstraintRepository weekDayConstraintRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks WeekDayConstraintServiceImpl weekDayConstraintService;
    private Lecturer testLecturer;
    private WeekDayConstraint testWeekDayConstraint;


    @BeforeEach
    void setUp() {

        testLecturer = new Lecturer();
        testLecturer.setId(21033105L);
        testLecturer.setName("John Doe");

        testWeekDayConstraint = new WeekDayConstraint();
        testWeekDayConstraint.setLecturer(testLecturer);
        testWeekDayConstraint.setMonday(true);
        testWeekDayConstraint.setTuesday(false);
        testWeekDayConstraint.setWednesday(true);
        testWeekDayConstraint.setThursday(false);
        testWeekDayConstraint.setFriday(true);
        
    }

    @Test
    @DisplayName("Test addWeekDayConstraint saves constraint")
    void testAddWeekDayConstraint() {
        // Mock the repository save method
        when(weekDayConstraintRepository.save(any(WeekDayConstraint.class))).thenReturn(testWeekDayConstraint);

        // Call the method
        WeekDayConstraint result = weekDayConstraintService.addWeekDayConstraint(testWeekDayConstraint);

        // Verify the result
        assertNotNull(result);
        assertEquals(testLecturer, result.getLecturer());
        assertTrue(result.isMonday());
        assertFalse(result.isTuesday());
        assertTrue(result.isWednesday());
        assertFalse(result.isThursday());
        assertTrue(result.isFriday());

        // Verify that the repository save method was called
        verify(weekDayConstraintRepository,times(1)).save(testWeekDayConstraint);
    }

    @Test
    @DisplayName("Test getWeekDayConstraintByLecturerId returns constraint")
    void testGetWeekDayConstraintByLecturerId() {
        // Mock the repository findByLecturer_Id method
        when(weekDayConstraintRepository.findByLecturer_Id(testLecturer.getId())).thenReturn(Optional.of(testWeekDayConstraint));

        // Call the method
        WeekDayConstraint result = weekDayConstraintService.getWeekDayConstraintByLecturerId(testLecturer.getId()).orElse(null);

        // Verify the result
        assertNotNull(result);
        assertEquals(testLecturer, result.getLecturer());
        assertTrue(result.isMonday());
        assertFalse(result.isTuesday());
        assertTrue(result.isWednesday());
        assertFalse(result.isThursday());
        assertTrue(result.isFriday());

        // Verify that the repository findByLecturer_Id method was called
        verify(weekDayConstraintRepository,times(1)).findByLecturer_Id(testLecturer.getId());
    }

    @Test
    @DisplayName("Test getWeekDayConstraintByLecturerId returns empty when not found")
    void testGetWeekDayConstraintByLecturerIdNotFound() {
        // Mock the repository findByLecturer_Id method to return empty
        when(weekDayConstraintRepository.findByLecturer_Id(testLecturer.getId())).thenReturn(Optional.empty());

        // Call the method
        Optional<WeekDayConstraint> result = weekDayConstraintService.getWeekDayConstraintByLecturerId(testLecturer.getId());

        // Verify the result
        assertFalse(result.isPresent());

        // Verify that the repository findByLecturer_Id method was called
        verify(weekDayConstraintRepository,times(1)).findByLecturer_Id(testLecturer.getId());
    }

}
