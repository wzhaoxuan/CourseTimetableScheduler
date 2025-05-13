package com.sunway.course.timetable.integration.repository;

import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.repository.LecturerRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
// @EntityScan("com.sunway.course.timetable.model")
class LecturerRepositoryTest {

    @Autowired
    private LecturerRepository lecturerRepository;

    // @Test
    // @DisplayName("Test save and findById")
    // void testFindById() {
    //     Lecturer lecturer = new Lecturer();
    //     lecturer.setName("John Doe");
    //     lecturer.setEmail("21033105@imail.sunway.edu.my");
    //     lecturer.setType("Full-Time");
    //     Lecturer saved = lecturerRepository.save(lecturer);

    //     Optional<Lecturer> result = lecturerRepository.findById(saved.getId());

    //     assertTrue(result.isPresent());
    //     assertEquals("John Doe", result.get().getName());
    // }

//     @Test
//     @DisplayName("Test findByName")
//     void testFindByName() {
//         Lecturer lecturer = new Lecturer();
//         lecturer.setName("Jane Smith");
//         lecturer.setType("Part-Time");
//         lecturerRepository.save(lecturer);

//         Optional<Lecturer> result = lecturerRepository.findByName("Jane Smith");

//         assertTrue(result.isPresent());
//         assertEquals("Part-Time", result.get().getType());
//     }

//     @Test
//     @DisplayName("Test findByName - Invalid Name")
//     void testFindByNameNotFound() {
//         Optional<Lecturer> result = lecturerRepository.findByName("Nonexistent Name");

//         assertFalse(result.isPresent());
//     }

//     @Test
//     @DisplayName("Test findByName - Empty Name")
//     void testFindByNameEmpty() {
//         Optional<Lecturer> result = lecturerRepository.findByName("");

//         assertFalse(result.isPresent());
//     }

//     @Test
//     @DisplayName("Test findByType")
//     void testFindByType() {
//         Lecturer l1 = new Lecturer();
//         l1.setName("Alice");
//         l1.setType("Full-Time");

//         Lecturer l2 = new Lecturer();
//         l2.setName("Bob");
//         l2.setType("Full-Time");

//         lecturerRepository.saveAll(List.of(l1, l2));

//         Optional<List<Lecturer>> result = lecturerRepository.findByType("Full-Time");

//         assertTrue(result.isPresent());
//         assertEquals(2, result.get().size());
//     }

//     @Test
//     @DisplayName("Test findByType - Invalid Type")
//     void testFindByTypeNotFound() {
//         Optional<List<Lecturer>> result = lecturerRepository.findByType("Nonexistent Type");

//         assertFalse(result.isPresent());
//     }

//     @Test
//     @DisplayName("Test findByType - Empty Type")
//     void testFindByTypeEmpty() {
//         Optional<List<Lecturer>> result = lecturerRepository.findByType("");

//         assertFalse(result.isPresent());
//     }
}
