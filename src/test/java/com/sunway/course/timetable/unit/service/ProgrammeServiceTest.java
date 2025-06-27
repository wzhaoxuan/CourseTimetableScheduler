package com.sunway.course.timetable.unit.service;

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
}
