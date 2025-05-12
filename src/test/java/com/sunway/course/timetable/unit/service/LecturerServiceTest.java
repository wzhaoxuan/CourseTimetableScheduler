package com.sunway.course.timetable.unit.service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.repository.LecturerRepository;
import com.sunway.course.timetable.service.LecturerServiceImpl;


@ExtendWith(MockitoExtension.class)
public class LecturerServiceTest {

    @Mock private LecturerRepository lecturerRepository;
    @InjectMocks private LecturerServiceImpl lecturerService;

    private Lecturer fullTimeLecturer, partTimeLecturer, teachingAsistant;
    private List<Lecturer> lecturers;

    @BeforeEach
    void setUp() {
        lecturers = new ArrayList<>();
        fullTimeLecturer = new Lecturer();
        fullTimeLecturer.setName("Chin Teck Min");
        fullTimeLecturer.setType("Full-Time");
        lecturers.add(fullTimeLecturer);

        partTimeLecturer = new Lecturer();
        partTimeLecturer.setName("Richard");
        partTimeLecturer.setType("Part-Time");
        lecturers.add(partTimeLecturer);

        teachingAsistant = new Lecturer();
        teachingAsistant.setName("John Doe");
        teachingAsistant.setType("Teaching Assistant");
        lecturers.add(teachingAsistant);
    }

    @Test
    @DisplayName("Test Get All Lecturers -- Success")
    void testGetAllLecturers(){
        when(lecturerRepository.findAll()).thenReturn(lecturers);

        List<Lecturer> result = lecturerService.getAllLecturers();

        assertEquals(3, result.size());
        verify(lecturerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test Get Lecturer By Name -- Success")
    void testGetLecturerByNameSuccess(){
        when(lecturerRepository.findByName("Chin Teck Min")).thenReturn(Optional.of(fullTimeLecturer));
        assertTrue(lecturerService.getLecturerByName("Chin Teck Min").isPresent());
        assertEquals("Chin Teck Min", lecturerService.getLecturerByName("Chin Teck Min").get().getName());
        verify(lecturerRepository, times(2)).findByName("Chin Teck Min");
    }

    @Test
    @DisplayName("Test Get Lecturer By ID -- Success")
    void testGetLecturerById(){
        when(lecturerRepository.findById(1L)).thenReturn(Optional.of(fullTimeLecturer));
        assertTrue(lecturerService.getLecturerById(1L).isPresent());
        verify(lecturerRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test Get Lecturers By Type -- Multiple Types")
    void testGetLecturersByType() {
        // Define the types to test
        String[] types = {"Full-Time", "Part-Time", "Teaching Assistant"};
        
        for (String type : types) {
            // Mock the repository for each type
            when(lecturerRepository.findByType(type)).thenReturn(Optional.of(lecturers.stream()
                .filter(l -> l.getType().equals(type))
                .collect(Collectors.toList())));

            // Call the service method and assert the result
            Optional<List<Lecturer>> result = lecturerService.getLecturersByType(type);
            assertTrue(result.isPresent());
            assertEquals(lecturers.stream()
            .filter(l -> l.getType().equals(type)).count(), result.get().size());

            // Verify the repository method was called once
            verify(lecturerRepository, times(1)).findByType(type);
        }
    }

    @Test
    @DisplayName("Should return empty list if no lecturers exist")
    void getAllLecturers_noLecturersExist_returnsEmptyList(){
        when(lecturerRepository.findAll()).thenReturn(Collections.emptyList());
        List<Lecturer> result = lecturerService.getAllLecturers();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list if no lecturer's name exist")
    void testGetLecturerByNameNotFound(){

        when(lecturerRepository.findByName("Chin Teck Min")).thenReturn(Optional.empty());
        assertTrue(lecturerService.getLecturerByName("Chin Teck Min").isEmpty());
        verify(lecturerRepository, times(1)).findByName("Chin Teck Min");
    }

    @Test
    @DisplayName("Should return empty when Full-Time lecturers not found")
    void getLecturersByType_fullTimeNotFound() {
        when(lecturerRepository.findByType("Full-Time")).thenReturn(Optional.empty());
        Optional<List<Lecturer>> result = lecturerService.getLecturersByType("Full-Time");
        assertTrue(result.isEmpty());
        verify(lecturerRepository, times(1)).findByType("Full-Time");
    }

    @Test
    @DisplayName("Should return empty when Part-Time lecturers not found")
    void getLecturersByType_partTimeNotFound() {
        when(lecturerRepository.findByType("Part-Time")).thenReturn(Optional.empty());
        Optional<List<Lecturer>> result = lecturerService.getLecturersByType("Part-Time");
        assertTrue(result.isEmpty());
        verify(lecturerRepository, times(1)).findByType("Part-Time");
    }

    @Test
    @DisplayName("Should return empty when Full-Time lecturers not found")
    void getLecturersByType_teachingAssistantNotFound() {
        when(lecturerRepository.findByType("Teaching Assistant")).thenReturn(Optional.empty());
        Optional<List<Lecturer>> result = lecturerService.getLecturersByType("Teaching Assistant");
        assertTrue(result.isEmpty());
        verify(lecturerRepository, times(1)).findByType("Teaching Assistant");
    }
}
