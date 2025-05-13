package com.sunway.course.timetable.edge.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.repository.ModuleRepository;
import com.sunway.course.timetable.service.ModuleServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ModuleEdgeCaseTest {

    @Mock private ModuleRepository moduleRepository;
    @InjectMocks private ModuleServiceImpl moduleService;

    @Test
    @DisplayName("Test Get Module By Credit Hour -- Negative")
    void testGetModuleByCreditHourNegative() {
        int invalidCreditHour = -1;
        String expectedMessage = "Credit hour must be greater than zero";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            moduleService.getModuleCreditHour(invalidCreditHour);
        });

        assertEquals(expectedMessage, exception.getMessage());
    }


}
