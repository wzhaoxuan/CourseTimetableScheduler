package com.sunway.course.timetable.service;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.exception.IdNotFoundException;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.repository.StudentRepository;
import com.sunway.course.timetable.interfaces.services.StudentService;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    @Override
    public Student addStudent(Student student) {
        return studentRepository.save(student);
    }

    @Override
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    @Override
    public Student updateStudent(Long id, Student student) {
        if (studentRepository.existsById(id)) {
            student.setId(id);
            return studentRepository.save(student);
        } else {
            throw new IdNotFoundException("Student not found with id: " + id);
        }
    }

    public List<Student> getStudentsByIds(Set<Long> ids) {
        return studentRepository.findAllById(ids);
    }


}
