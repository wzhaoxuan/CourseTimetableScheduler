package com.sunway.course.timetable.edge.repository;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.sunway.course.timetable.repository.LecturerRepository;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class LecturerRepoEdgeCaseTest {

    @Autowired
    private LecturerRepository lecturerRepository;

    // @Test
    // @DisplayName("Test findByName - Null Name")
    // void testFindByNameNull() {
    //     assertThrows(IllegalArgumentException.class, () -> {
    //         lecturerRepository.findByName(null);
    //     });
    // }

    // @Test
    // @DisplayName("Test findByType - Null Type")
    // void testFindByTypeNull() {
    //     assertThrows(IllegalArgumentException.class, () -> {
    //         lecturerRepository.findByType(null);
    //     });
    // }

}
