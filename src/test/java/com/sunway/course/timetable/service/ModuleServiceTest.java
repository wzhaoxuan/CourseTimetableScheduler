package com.sunway.course.timetable.service;
import com.sunway.course.timetable.exception.NullValueException;
import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.repository.ModuleRepository;

import java.util.ArrayList;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
;

@ExtendWith(MockitoExtension.class)
public class ModuleServiceTest {

    @Mock private ModuleRepository moduleRepository;
    @InjectMocks private ModuleServiceImpl moduleService;

    @Test
    @DisplayName("Test Get All Modules -- Success")
    void testGetAllModules() {
        List<Module> modules = new ArrayList<>();

        Module module = new Module();
        module.setName("Software Engineering");
        module.setCreditHour(3);
        modules.add(module);

        Module module2 = new Module();
        module2.setName("Database Systems");
        module2.setCreditHour(3);
        modules.add(module2);

        when(moduleRepository.findAll()).thenReturn(modules);

        List<Module> result = moduleService.getAllModules();

        assertEquals(2, result.size());
        verify(moduleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test Get All Modules -- Empty List")
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
        Module module = new Module();
        module.setId(1L);
        module.setName("Software Engineering");
        module.setCreditHour(3);

        when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));

        Optional<Module> result = moduleService.getModuleById(1L);

        assertTrue(result.isPresent());
        assertEquals("Software Engineering", result.get().getName());
        verify(moduleRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test Get Module By ID -- Not Found")
    void testGetModuleByIdNotFound() {
        when(moduleRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Module> result = moduleService.getModuleById(1L);

        assertTrue(result.isEmpty());
        verify(moduleRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test Get Module By Name -- Success")
    void testGetModuleByName() {
        Module module = new Module();
        module.setName("Software Engineering");
        module.setCreditHour(3);

        when(moduleRepository.findByName("Software Engineering")).thenReturn(Optional.of(module));

        Optional<Module> result = moduleService.getModuleByName("Software Engineering");

        assertTrue(result.isPresent());
        assertEquals("Software Engineering", result.get().getName());
        verify(moduleRepository, times(1)).findByName("Software Engineering");
    }

    @Test
    @DisplayName("Test Get Module By Name -- Not Found")
    void testGetModuleByNameNotFound() {
        when(moduleRepository.findByName("Software Engineering")).thenReturn(Optional.empty());

        Optional<Module> result = moduleService.getModuleByName("Software Engineering");

        assertTrue(result.isEmpty());
        verify(moduleRepository, times(1)).findByName("Software Engineering");
    }

    @Test
    @DisplayName("Test Get Module By Credit Hour -- Success")
    void testGetModuleByCreditHour() {
        Module module = new Module();
        module.setCreditHour(3);

        when(moduleRepository.findByCreditHour(3)).thenReturn(Optional.of(module));

        Optional<Module> result = moduleService.getModuleCreditHour(3);

        assertTrue(result.isPresent());
        assertEquals(3, result.get().getCreditHour());
        verify(moduleRepository, times(1)).findByCreditHour(3);
    }

    @Test
    @DisplayName("Test Get Module By Credit Hour -- Not Found")
    void testGetModuleByCreditHourNotFound() {
        when(moduleRepository.findByCreditHour(3)).thenReturn(Optional.empty());

        Optional<Module> result = moduleService.getModuleCreditHour(3);

        assertTrue(result.isEmpty());
        verify(moduleRepository, times(1)).findByCreditHour(3);
    }

    @Test
    @DisplayName("Test Get Module By Credit Hour -- Nagative")
    void testGetModuleByCreditHourNegative() {

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            moduleService.getModuleCreditHour(-1);
        });

        assertEquals("Credit hour must be greater than zero", exception.getMessage());
    }
}
