package com.sunway.course.timetable.unit.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.model.programme.Programme;
import com.sunway.course.timetable.model.programme.ProgrammeId;
import com.sunway.course.timetable.repository.ProgrammeRepository;
import com.sunway.course.timetable.service.ProgrammeServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ProgrammeServiceTest{

    @Mock
    private ProgrammeRepository programmeRepository;

    @InjectMocks
    private ProgrammeServiceImpl programmeService;

    private Programme programme;
    private ProgrammeId programmeId;

    @BeforeEach
    void setUp() {
        programmeId = new ProgrammeId("1", 2L, "3");
        programme = new Programme();
        programme.setProgrammeId(programmeId);
        programme.setName("Computer Science");
        programme.setYear(1);
        programme.setIntake("Jan");
        programme.setSemester(1);
    }

    @Test
    @DisplayName("Get Programme By ID - Found")
    void testGetProgrammeByIdFound() {
        when(programmeRepository.findById(programmeId)).thenReturn(Optional.of(programme));

        Optional<Programme> result = programmeService.getProgrammeById(programmeId);

        assertTrue(result.isPresent());
        assertEquals("Computer Science", result.get().getName());
        verify(programmeRepository, times(1)).findById(programmeId);
    }

    @Test
    @DisplayName("Get Programme By ID - Not Found")
    void testGetProgrammeByIdNotFound() {
        when(programmeRepository.findById(programmeId)).thenReturn(Optional.empty());

        Optional<Programme> result = programmeService.getProgrammeById(programmeId);

        assertFalse(result.isPresent());
        verify(programmeRepository, times(1)).findById(programmeId);
    }

    @Test
    @DisplayName("Save Programme")
    void testSaveProgramme() {
        when(programmeRepository.save(programme)).thenReturn(programme);

        Programme saved = programmeService.saveProgramme(programme);

        assertNotNull(saved);
        assertEquals("Computer Science", saved.getName());
        verify(programmeRepository, times(1)).save(programme);
    }

    @Test
    @DisplayName("Delete Programme")
    void testDeleteProgramme() {
        doNothing().when(programmeRepository).deleteById(programmeId);

        programmeService.deleteProgramme(programmeId);

        verify(programmeRepository, times(1)).deleteById(programmeId);
    }

    @Test
    @DisplayName("Get Programme By Name - Found")
    void testGetProgrammeByNameFound() {
        when(programmeRepository.findByName("Computer Science")).thenReturn(Optional.of(programme));

        Optional<Programme> result = programmeService.getProgrammeByName("Computer Science");

        assertTrue(result.isPresent());
        assertEquals("Computer Science", result.get().getName());
    }

    @Test
    @DisplayName("Get Programme By Name - Not Found")
    void testGetProgrammeByNameNotFound() {
        when(programmeRepository.findByName("Unknown")).thenReturn(Optional.empty());

        Optional<Programme> result = programmeService.getProgrammeByName("Unknown");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Get Programmes By Year")
    void testGetProgrammesByYear() {
        when(programmeRepository.findByYear(1)).thenReturn(List.of(programme));

        List<Programme> result = programmeService.getProgrammesByYear(1);

        assertEquals(1, result.size());
        assertEquals("Computer Science", result.get(0).getName());
    }

    @Test
    @DisplayName("Return empty list if programme not found by year")
    void testGetProgrammesByYearNotFound() {
        when(programmeRepository.findByYear(2)).thenReturn(List.of());

        List<Programme> result = programmeService.getProgrammesByYear(2);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Get Programmes By Intake")
    void testGetProgrammesByIntake() {
        when(programmeRepository.findByIntake("Jan")).thenReturn(List.of(programme));

        List<Programme> result = programmeService.getProgrammesByIntake("Jan");

        assertEquals(1, result.size());
        assertEquals("Computer Science", result.get(0).getName());
    }

    @Test
    @DisplayName("Return empty list if programme not found by intake")
    void testGetProgrammesByIntakeNotFound() {
        when(programmeRepository.findByIntake("Feb")).thenReturn(List.of());

        List<Programme> result = programmeService.getProgrammesByIntake("Feb");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Get Programmes By Semester")
    void testGetProgrammesBySemester() {
        when(programmeRepository.findBySemester(1)).thenReturn(List.of(programme));

        List<Programme> result = programmeService.getProgrammesBySemester(1);

        assertEquals(1, result.size());
        assertEquals("Computer Science", result.get(0).getName());
    }

    @Test
    @DisplayName("Return empty list if programme not found by semester")
    void testGetProgrammesBySemesterNotFound() {
        when(programmeRepository.findBySemester(2)).thenReturn(List.of());

        List<Programme> result = programmeService.getProgrammesBySemester(2);

        assertTrue(result.isEmpty());
    }
}
