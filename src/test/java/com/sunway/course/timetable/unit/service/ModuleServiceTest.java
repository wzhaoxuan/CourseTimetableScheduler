package com.sunway.course.timetable.unit.service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.repository.ModuleRepository;
import com.sunway.course.timetable.service.ModuleServiceImpl;


@ExtendWith(MockitoExtension.class)
public class ModuleServiceTest {

    @Mock private ModuleRepository moduleRepository;
    @InjectMocks private ModuleServiceImpl moduleService;

    private Module module, module2;
    private List<Module> modules;
    
    @BeforeEach
    void setUp() {
        modules = new ArrayList<>();
        module = new Module();
        module.setId("BIS2104");
        module.setName("Software Engineering");
        module.setCreditHour(3);
        modules.add(module);

        module2 = new Module();
        module2.setCreditHour(3);
        module2.setName("Database Systems");
        modules.add(module2);

    }

    @Test
    @DisplayName("Test Get All Modules -- Success")
    void testGetAllModules() {

        when(moduleRepository.findAll()).thenReturn(modules);

        List<Module> result = moduleService.getAllModules();

        assertEquals(2, result.size());
        verify(moduleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list if no lecturers exist")
    void testGetAllModulesEmptyList() {
        List<Module> modules = new ArrayList<>();

        when(moduleRepository.findAll()).thenReturn(modules);
        List<Module> result = moduleService.getAllModules();

        assertEquals(0, result.size());
        verify(moduleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test Get Module By ID -- Success")
    void testGetModuleById() {
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));

        Optional<Module> result = moduleService.getModuleById(1L);

        assertTrue(result.isPresent());
        assertEquals("Software Engineering", result.get().getName());
        verify(moduleRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty list if module not found by ID")
    void testGetModuleByIdNotFound() {
        when(moduleRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Module> result = moduleService.getModuleById(1L);

        assertTrue(result.isEmpty());
        verify(moduleRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test Get Module By Name")
    void testGetModuleByName() {
        when(moduleRepository.findByName("Software Engineering")).thenReturn(Optional.of(module));

        Optional<Module> result = moduleService.getModuleByName("Software Engineering");

        assertTrue(result.isPresent());
        assertEquals("Software Engineering", result.get().getName());
        verify(moduleRepository, times(1)).findByName("Software Engineering");
    }

    @Test
    @DisplayName("Should return empty list if module not found by Name")
    void testGetModuleByNameNotFound() {
        when(moduleRepository.findByName("Software Engineering")).thenReturn(Optional.empty());

        Optional<Module> result = moduleService.getModuleByName("Software Engineering");

        assertTrue(result.isEmpty());
        verify(moduleRepository, times(1)).findByName("Software Engineering");
    }

    @Test
    @DisplayName("Test Get Module By Credit Hour")
    void testGetModuleByCreditHour() {
        when(moduleRepository.findByCreditHour(3)).thenReturn(Optional.of(modules));

        Optional<List<Module>> result = moduleService.getModuleCreditHour(3);

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        verify(moduleRepository, times(1)).findByCreditHour(3);
    }

    @Test
    @DisplayName("Test Get Module By Credit Hour -- Positive")
    void testGetModuleByCreditHourPositive() {
        int validCreditHour = 3;

        // Mock the repository to return the expected list
        when(moduleRepository.findByCreditHour(validCreditHour)).thenReturn(Optional.of(modules));

        // Call the method
        Optional<List<Module>> result = moduleService.getModuleCreditHour(validCreditHour);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        assertEquals("Software Engineering", result.get().get(0).getName());
        verify(moduleRepository, times(1)).findByCreditHour(validCreditHour);
    }
    

    @Test
    @DisplayName("Return empty list if module not found by credit hour")
    void testGetModuleByCreditHourNotFound() {
        when(moduleRepository.findByCreditHour(3)).thenReturn(Optional.empty());

        Optional<List<Module>> result = moduleService.getModuleCreditHour(3);

        assertTrue(result.isEmpty());
        verify(moduleRepository, times(1)).findByCreditHour(3);
    }
}
