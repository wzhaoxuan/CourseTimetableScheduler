package com.sunway.course.timetable.unit.service.preprocess;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.SubjectPlanInfo;
import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;
import com.sunway.course.timetable.model.assignment.SessionGroupMetaData;
import com.sunway.course.timetable.service.processor.preprocessing.SessionGroupPreprocessorService;

public class SessionGroupPreprocessorServiceTest {

    private SessionGroupPreprocessorService service;

    @BeforeEach
    void setUp() {
        service = new SessionGroupPreprocessorService();
    }

    @Test
    @DisplayName("prepareSessionGroupMetadata with lecture only produces one lecture group and tracks semester")
    void testLectureOnly() {
        // Arrange
        Module module = new Module("M1", "Module 1", 3);
        List<String> emptyTutors = List.of();
        SubjectPlanInfo plan = new SubjectPlanInfo(
            "M1", "Module 1",
            /* lecture */ true,
            /* practical */ false,
            /* tutorial */ false,
            /* workshop */ false,
            /* totalStudents */ 5,
            /* mainLecturer */ "DrA",
            emptyTutors, emptyTutors, emptyTutors
        );

        Set<Student> students = new HashSet<>();
        for (long i = 1; i <= 5; i++) {
            Student s = new Student();
            s.setId(i);
            students.add(s);
        }
        Map<Long, Integer> studentSemesterMap = new HashMap<>();
        students.forEach(s -> studentSemesterMap.put(s.getId(), 1));

        ModuleAssignmentData data = new ModuleAssignmentData(
            plan, module, List.of(), students
        );

        // Act
        List<SessionGroupMetaData> metaList = service.prepareSessionGroupMetadata(
            data, studentSemesterMap
        );

        // Assert: exactly one lecture group
        assertEquals(1, metaList.size(), "Should create only the lecture group");
        SessionGroupMetaData lectureMeta = metaList.get(0);
        assertEquals("Lecture", lectureMeta.getType());
        assertEquals("M1-Lecture-G1", lectureMeta.getTypeGroup());
        assertEquals(5, lectureMeta.getTotalStudents(), "Should have 5 students in lecture group");
        assertEquals("DrA", lectureMeta.getLecturerName());

        // Semester should be tracked
        List<Integer> semesters = service.getSemestersForModule("M1");
        assertEquals(List.of(1), semesters, "Semester 1 should be recorded");
    }

    @Test
    @DisplayName("prepareSessionGroupMetadata with practical and tutorial creates correct groups and cycles tutors")
    void testPracticalAndTutorialGroups() {
        // Arrange: 70 students → groupCount = ceil(70/35) = 2
        Module module = new Module("M2", "Module 2", 3);
        List<String> tutors = List.of("T1", "T2");

        SubjectPlanInfo plan = new SubjectPlanInfo(
            "M2", "Module 2",
            /* lecture */ false,
            /* practical */ true,
            /* tutorial */ true,
            /* workshop */ false,
            /* totalStudents */ 70,
            /* mainLecturer */ "DrB",
            tutors, tutors, List.of()
        );

        Set<Student> students = new HashSet<>();
        for (long i = 1; i <= 70; i++) {
            Student s = new Student();
            s.setId(i);
            students.add(s);
        }
        Map<Long, Integer> studentSemesterMap = new HashMap<>();
        students.forEach(s -> studentSemesterMap.put(s.getId(), 2));

        ModuleAssignmentData data = new ModuleAssignmentData(
            plan, module, List.of(), students
        );

        // Act
        List<SessionGroupMetaData> metaList = service.prepareSessionGroupMetadata(
            data, studentSemesterMap
        );

        // Assert: two practical + two tutorial = 4 groups
        assertEquals(4, metaList.size(), "Should create 2 practical + 2 tutorial groups");
        long practicalCount = metaList.stream()
            .filter(m -> m.getType().equals("Practical"))
            .count();
        long tutorialCount = metaList.stream()
            .filter(m -> m.getType().equals("Tutorial"))
            .count();

        assertEquals(2, practicalCount);
        assertEquals(2, tutorialCount);

        // Check tutor cycling: first tutorial tutor should be "T1"
        SessionGroupMetaData firstTut =
            metaList.stream()
                    .filter(m -> m.getType().equals("Tutorial"))
                    .findFirst()
                    .orElseThrow();
        assertEquals("M2-Tutorial-G1", firstTut.getTypeGroup());
        assertEquals("T1", firstTut.getLecturerName());

        // Semester should be tracked
        assertEquals(List.of(2), service.getSemestersForModule("M2"));
    }

    @Test
    @DisplayName("reset clears all tracked semesters")
    void testResetClearsState() {
        // Pre-populate via a dummy call
        Module module = new Module("M3", "Module 3", 3);
        SubjectPlanInfo plan = new SubjectPlanInfo(
            "M3", "Module 3",
            true, false, false, false,
            1, "DrC", List.of(), List.of(), List.of()
        );
        Student s = new Student(); s.setId(99L);
        ModuleAssignmentData data = new ModuleAssignmentData(
            plan, module, List.of(), Set.of(s)
        );
        Map<Long, Integer> semMap = Map.of(99L, 3);

        service.prepareSessionGroupMetadata(data, semMap);
        assertFalse(service.getSemestersForModule("M3").isEmpty(),
            "State should contain semester 3");

        service.reset();
        assertTrue(service.getSemestersForModule("M3").isEmpty(),
            "After reset, no semesters should be tracked");
    }
}
