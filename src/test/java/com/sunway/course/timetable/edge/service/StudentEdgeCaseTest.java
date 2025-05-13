package com.sunway.course.timetable.edge.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.repository.StudentRepository;
import com.sunway.course.timetable.service.StudentServiceImpl;

@ExtendWith(MockitoExtension.class)
public class StudentEdgeCaseTest {

    @Mock private StudentRepository studentRepository;
    @InjectMocks private StudentServiceImpl studentService;
    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(21033105L);
        student.setStudentName("John Doe");
    }

    @Test
    @DisplayName("Test Add Student with Null Input")
    void testAddStudentWithNullInput() {
        doThrow(new IllegalArgumentException("Null input"))
            .when(studentRepository).save(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            studentService.addStudent(null);
        });

        assertEquals("Null input", exception.getMessage());
        verify(studentRepository).save(null);
    }

    @Test
    @DisplayName("Test Delete on NUll Student ID")
    void testDeleteNonExistentStudent() {
        doThrow(new IllegalArgumentException("Student not found")).when(studentRepository).deleteById(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            studentService.deleteStudent(null);
        });

        assertEquals("Student not found", exception.getMessage());
        verify(studentRepository).deleteById(null);
    }

}
