package com.sunway.course.timetable.unit.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.exception.IdNotFoundException;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.repository.StudentRepository;
import com.sunway.course.timetable.service.StudentServiceImpl;

@ExtendWith(MockitoExtension.class)
public class StudentSeviceTest {

    @Mock private StudentRepository studentRepository;
    @InjectMocks private StudentServiceImpl studentService;
    
    private Student student, updatedStudent;
    
    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(21033105L);
        student.setStudentName("John Doe");

        updatedStudent = new Student();
        updatedStudent.setId(21033108L);
        updatedStudent.setStudentName("Bruno Mars");
    }

    @Test
    @DisplayName("Test Get All Students")
    void testGetAllStudents() {
        
        when(studentRepository.findAll()).thenReturn(Arrays.asList(student));
        
        List<Student> result = studentService.getAllStudents();
        
        assertEquals(1, result.size());
        verify(studentRepository).findAll();
    }

    @Test
    @DisplayName("Test Get Student By ID - Success")
    void testGetStudentById() {

        
        when(studentRepository.findById(21033105L)).thenReturn(Optional.of(student));
        
        Optional<Student> result = studentService.getStudentById(21033105L);
        
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getStudentName());
        verify(studentRepository).findById(21033105L);
    }

    @Test
    @DisplayName("Test Get Student By ID - Not Found")
    void testGetStudentByIdNotFound() {
        when(studentRepository.findById(21033105L)).thenReturn(Optional.empty());
        
        Optional<Student> result = studentService.getStudentById(21033105L);
        
        assertTrue(result.isEmpty());
        verify(studentRepository).findById(21033105L);
    }       

    @Test
    @DisplayName("Test Add Student")
    void testAddStudent() {        
        when(studentRepository.save(student)).thenReturn(student);
        
        Student result = studentService.addStudent(student);
        
        assertEquals("John Doe", result.getStudentName());
        verify(studentRepository).save(student);
    }

    @Test
    @DisplayName("Test Delete Student")
    void testDeleteStudent() {
        doNothing().when(studentRepository).deleteById(21033105L);
        
        studentService.deleteStudent(21033105L);
        
        verify(studentRepository).deleteById(21033105L);
    }

    @Test
    @DisplayName("Test Update Student - Success")
    void testUpdateStudent() {
        when(studentRepository.existsById(student.getId())).thenReturn(true);
        when(studentRepository.save(updatedStudent)).thenReturn(updatedStudent);

        Student result = studentService.updateStudent(student.getId(), updatedStudent);

        assertEquals("Bruno Mars", result.getStudentName());
        verify(studentRepository).save(updatedStudent);
        verify(studentRepository).existsById(student.getId());
    }

    @Test
    @DisplayName("Test Update Student - Not Found")
    void testUpdateStudentNotFound() {

        when(studentRepository.existsById(student.getId())).thenReturn(false);

        IdNotFoundException exception = assertThrows(IdNotFoundException.class, () -> {
            studentService.updateStudent(student.getId(), updatedStudent);
        });

        assertEquals("Student not found with id: 21033105", exception.getMessage());
        verify(studentRepository).existsById(student.getId());
    }


}
