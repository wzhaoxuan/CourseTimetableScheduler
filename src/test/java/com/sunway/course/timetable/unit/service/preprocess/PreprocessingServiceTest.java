package com.sunway.course.timetable.unit.service.preprocess;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.SubjectPlanInfo;
import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;
import com.sunway.course.timetable.model.assignment.ModuleSem;
import com.sunway.course.timetable.model.assignment.StudentSem;
import com.sunway.course.timetable.model.programme.Programme;
import com.sunway.course.timetable.model.programme.ProgrammeId;
import com.sunway.course.timetable.repository.ModuleRepository;
import com.sunway.course.timetable.repository.ProgrammeRepository;
import com.sunway.course.timetable.result.PreprocessingResult;
import com.sunway.course.timetable.service.excelReader.ModuleSemExcelReaderService;
import com.sunway.course.timetable.service.excelReader.StudentSemExcelReaderService;
import com.sunway.course.timetable.service.excelReader.SubjectPlanExcelReaderService;
import com.sunway.course.timetable.service.processor.preprocessing.PreprocessingService;

@ExtendWith(MockitoExtension.class)
public class PreprocessingServiceTest {

    @Mock
    private SubjectPlanExcelReaderService subjectPlanExcelReaderService;

    @Mock
    private ModuleSemExcelReaderService moduleSemExcelReaderService;

    @Mock
    private StudentSemExcelReaderService studentSemExcelReaderService;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private ProgrammeRepository programmeRepository;

    @InjectMocks
    private PreprocessingService preprocessingService;

    @Test
    @DisplayName("When all inputs empty, result contains no assignments or students")
    void testPreprocessWithNoData() throws Exception {
        when(subjectPlanExcelReaderService.readExcelFile(anyString()))
            .thenReturn(Collections.emptyList());
        when(moduleSemExcelReaderService.readModuleSemExcelFile(anyString()))
            .thenReturn(Collections.emptyMap());
        when(studentSemExcelReaderService.readStudentSemExcelFile(anyString()))
            .thenReturn(Collections.emptyMap());

        PreprocessingResult result = preprocessingService.preprocessModuleAndStudents(
            "subjects.xlsx", "moduleSem.xlsx", "studentSem.xlsx"
        );

        assertTrue(result.getModuleAssignmentDataList().isEmpty(), "No ModuleAssignmentData expected");
        assertTrue(result.getStudentProgrammeMap().isEmpty(), "No programme mappings expected");
        assertTrue(result.getStudentSemesterMap().isEmpty(), "No semester mappings expected");
    }

    @Test
    @DisplayName("Single module + single student → one assignment and correct maps")
    void testPreprocessSingleModuleStudent() throws Exception {
        // --- 1) Stub subject plans: one entry for module "M1" ---
        SubjectPlanInfo plan = new SubjectPlanInfo(
            "M1", "Mod One",
            true, false, false, false,
            30, "Dr X",
            List.of(), List.of(), List.of()
        );
        when(subjectPlanExcelReaderService.readExcelFile(anyString()))
            .thenReturn(List.of(plan));

        // --- 2) Stub moduleSem: semester 1 contains M1 ---
        ModuleSem ms = new ModuleSem("M1");
        when(moduleSemExcelReaderService.readModuleSemExcelFile(anyString()))
            .thenReturn(Map.of(1, List.of(ms)));

        // --- 3) Stub studentSem: semester 1 contains student 100 with programme “BCS” ---
        StudentSem ss = new StudentSem(100, "BCS", 1);
        when(studentSemExcelReaderService.readStudentSemExcelFile(anyString()))
            .thenReturn(Map.of(1, List.of(ss)));

        // --- 4) Stub ModuleRepository.findById("M1") → a real Module object ---
        Module module = new Module("M1", "Mod One", 3);
        when(moduleRepository.findById("M1"))
            .thenReturn(Optional.of(module));

        // --- 5) Stub ProgrammeRepository.findByModuleId("M1") →
        //     a single Programme with programmeId="BCS" and student 100
        Student student = new Student();
        student.setId(100L);

        // Create a fake ProgrammeId and Programme
        ProgrammeId progId = new ProgrammeId();
        progId.setId("BCS");

        Programme programme = new Programme();
        programme.setProgrammeId(progId);
        programme.setStudent(student);

        when(programmeRepository.findByModuleId("M1"))
            .thenReturn(List.of(programme));

        // --- 6) Execute ---
        PreprocessingResult result = preprocessingService.preprocessModuleAndStudents(
            "s.xlsx", "ms.xlsx", "ss.xlsx"
        );

        // --- 7) Verify one assignment data entry ---
        List<ModuleAssignmentData> assignments = result.getModuleAssignmentDataList();
        assertEquals(1, assignments.size(), "Exactly one ModuleAssignmentData expected");

        ModuleAssignmentData data = assignments.get(0);
        assertSame(plan, data.getSubjectPlanInfo());
        assertSame(module, data.getModule());

        // The student set should contain the Student(100)
        Set<Student> eligible = data.getEligibleStudents();
        assertEquals(1, eligible.size());
        assertTrue(eligible.contains(student));

        // --- 8) Verify programme & semester maps ---
        Map<Long, String> progMap = result.getStudentProgrammeMap();
        Map<Long, Integer> semMap  = result.getStudentSemesterMap();

        assertEquals(Map.of(100L, "BCS"), progMap,
            "StudentProgrammeMap should map 100L→’BCS’");
        assertEquals(Map.of(100L, 1), semMap,
            "StudentSemesterMap should map 100L→semester 1");
    }
}

