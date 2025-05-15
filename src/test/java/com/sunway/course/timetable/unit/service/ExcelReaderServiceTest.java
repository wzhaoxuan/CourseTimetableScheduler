package com.sunway.course.timetable.unit.service;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.model.SubjectPlanInfo;
import com.sunway.course.timetable.service.ExcelReaderService;
import com.sunway.course.timetable.service.generator.ModuleGenerator;

@ExtendWith(MockitoExtension.class)
public class ExcelReaderServiceTest {

    @Mock private ModuleGenerator moduleGenerator;
    @InjectMocks private ExcelReaderService excelReaderService;
    
    @BeforeEach
    public void setUp() {
    }


    @Test
    public void testReadExcelFile() throws Exception{
        String filePath = "src/test/resource/file/SubjectPlanTest.xlsx";
        List<SubjectPlanInfo> subjectPlanInfos = excelReaderService.readExcelFile(filePath);

        assertEquals(2, subjectPlanInfos.size(), "Expected 2 SubjectPlanInfo objects in the list");

        SubjectPlanInfo firstSubjectPlanInfo = subjectPlanInfos.get(0);
        assertEquals("CSC3209", firstSubjectPlanInfo.getSubjectCode(), "Subject code should match");
        assertEquals("Software Architecture and Design Patterns", firstSubjectPlanInfo.getSubjectName(), "Subject name should match");
        assertEquals(true, firstSubjectPlanInfo.hasLecture(), "Lecture should be true");
        assertEquals(true, firstSubjectPlanInfo.hasPractical(), "Practical should be false");
        assertEquals(false, firstSubjectPlanInfo.hasTutorial(), "Tutorial should be true");
        assertEquals(false, firstSubjectPlanInfo.hasWorkshop(), "Workshop should be false");
        assertEquals(17, firstSubjectPlanInfo.getTotalStudents(), "Total students should match");
        assertEquals("Dr Muhammed Basheer Jasser", firstSubjectPlanInfo.getMainLecturer(), "Main lecturer should match");
        assertEquals("Dr Muhammed Basheer Jasser", firstSubjectPlanInfo.getPracticalTutor(), "Practical tutor should match");
        assertEquals("", firstSubjectPlanInfo.getTutorialTutor(), "Tutorial tutor should match");
        assertEquals("", firstSubjectPlanInfo.getWorkshopTutor(), "Workshop tutor should match");
    }

    @Test
    public void testReadExcelFileWithEmptyFile() throws Exception {
        File emptyFile = File.createTempFile("empty", ".xlsx");
        try(XSSFWorkbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("Sheet1");
            try(FileOutputStream fos = new FileOutputStream(emptyFile)) {
                workbook.write(fos);
            }
        }

        List<SubjectPlanInfo> subjectPlanInfos = excelReaderService.readExcelFile(emptyFile.getAbsolutePath());
        assertTrue(subjectPlanInfos.isEmpty(), "Expected empty list for empty file");
    }

    @Test
    public void testReadExcelFileWithNonExistentFile() throws Exception {
        assertThrows(FileNotFoundException.class, () -> {
            String filePath = "src/test/resource/file/NonExistentFile.xlsx";
            excelReaderService.readExcelFile(filePath);
        }, "Could not find the file");
    }
}
