package com.sunway.course.timetable.service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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


@ExtendWith(MockitoExtension.class)
public class LecturerServiceTest {

    @Mock private LecturerRepository lecturerRepository;
    @InjectMocks private LecturerServiceImpl lecturerService;

    @Test
    @DisplayName("Test Get All Lecturers -- Success")
    void testGetAllLecturers(){
        List<Lecturer> lecturers = new ArrayList<>();

        Lecturer lecturer = new Lecturer();
        lecturer.setName("John Doe");
        lecturer.setType("Full-Time");
        lecturers.add(lecturer);

        Lecturer lecturer2 = new Lecturer();
        lecturer2.setName("Jane Smith");
        lecturer2.setType("Part-Time");
        lecturers.add(lecturer2);

        when(lecturerRepository.findAll()).thenReturn(lecturers);

        List<Lecturer> result = lecturerService.getAllLecturers();

        assertEquals(2, result.size());
        verify(lecturerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test Get All Lecturer -- Empty List")
    void testGetAllLecturersEmptyList(){
        List<Lecturer> lecturers = new ArrayList<>();

        when(lecturerRepository.findAll()).thenReturn(lecturers);
        List<Lecturer> result = lecturerService.getAllLecturers();

        assertEquals(0, result.size());
        verify(lecturerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test Get Lecturer By Name -- Success")
    void testGetLecturerByNameSuccess(){
        Lecturer lecturer = new Lecturer();
        lecturer.setName("Chin Teck Min");

        when(lecturerRepository.findByName("Chin Teck Min")).thenReturn(Optional.of(lecturer));
        assertTrue(lecturerService.getLecturerByName("Chin Teck Min").isPresent());
        assertEquals("Chin Teck Min", lecturerService.getLecturerByName("Chin Teck Min").get().getName());
        verify(lecturerRepository, times(2)).findByName("Chin Teck Min");
    }

    @Test
    @DisplayName("Test Get Lecturer By Name -- Not Found")
    void testGetLecturerByNameNotFound(){

        when(lecturerRepository.findByName("Chin Teck Min")).thenReturn(Optional.empty());
        assertTrue(lecturerService.getLecturerByName("Chin Teck Min").isEmpty());
        verify(lecturerRepository, times(1)).findByName("Chin Teck Min");

    }

    @Test
    @DisplayName("Test Get Lecturer By ID -- Success")
    void testGetLecturerById(){
        Lecturer lecturer = new Lecturer();
        lecturer.setId(1L);
        lecturer.setName("Chin Teck Min");

        when(lecturerRepository.findById(1L)).thenReturn(Optional.of(lecturer));
        assertTrue(lecturerService.getLecturerById(1L).isPresent());
        verify(lecturerRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test Get Lecturers By Type -- Multiple Types")
    void testGetLecturersByType() {
        List<Lecturer> lecturers = new ArrayList<>();

        // Create lecturers
        Lecturer fullTimeLecturer = new Lecturer();
        fullTimeLecturer.setName("Chin Teck Min");
        fullTimeLecturer.setType("Full-Time");
        lecturers.add(fullTimeLecturer);

        Lecturer partTimeLecturer = new Lecturer();
        partTimeLecturer.setName("Richard");
        partTimeLecturer.setType("Part-Time");
        lecturers.add(partTimeLecturer);

        Lecturer teachingAsistant = new Lecturer();
        teachingAsistant.setName("John Doe");
        teachingAsistant.setType("Teaching Assistant");
        lecturers.add(teachingAsistant);

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
    @DisplayName("Test Get Lecturers By Type -- Empty List")
    void testGetLecturersByTypeEmptyList() {
        String[] types = {"Full-Time", "Part-Time", "Teaching Assistant"};

        for(String type : types) {

            when(lecturerRepository.findByType(type)).thenReturn(Optional.empty());
            Optional<List<Lecturer>> result = lecturerService.getLecturersByType(type);
            assertTrue(result.isEmpty());
            verify(lecturerRepository, times(1)).findByType(type);

        }
    }
}
