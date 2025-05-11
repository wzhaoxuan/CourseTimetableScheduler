package com.sunway.course.timetable.interfaces.services;

import java.util.List;
import java.util.Optional;

import com.sunway.course.timetable.model.Student;

public interface StudentService {
    List<Student> getAllStudents();
    Optional<Student> getStudentById(Long id);
    Student addStudent(Student student);
    void deleteStudent(Long id);
    Student updateStudent(Long id, Student student);

}
