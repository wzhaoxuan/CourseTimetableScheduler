package com.sunway.course.timetable.unit.excelReader;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.model.SubjectPlanInfo;
import com.sunway.course.timetable.service.excelReader.SubjectPlanExcelReaderService;
import com.sunway.course.timetable.service.generator.ModuleGenerator;

@ExtendWith(MockitoExtension.class)
public class SubjectPlanExcelReaderServiceTest {

    @Mock
    private ModuleGenerator moduleGenerator;

    @InjectMocks
    private SubjectPlanExcelReaderService service;

    @Test
    @DisplayName("readExcelFile parses rows and calls ModuleGenerator appropriately")
    void testReadExcelFile(@TempDir Path tempDir) throws Exception {
        // 1) Create a workbook in memory
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Sheet1");
        // Header row
        Row header = sheet.createRow(0);
        String[] cols = {
            "Subject Code", "Subject Name", "Lecture", "Practical",
            "Tutorial", "Workshop", "Total Estimated Students",
            "Lecturer", "PracticalTutor", "TutorialTutor", "WorkshopTutor"
        };
        for (int c = 0; c < cols.length; c++) {
            header.createCell(c).setCellValue(cols[c]);
        }
        // Data row 1
        Row r1 = sheet.createRow(1);
        r1.createCell(0).setCellValue("CSC1001");
        r1.createCell(1).setCellValue("Intro to CS");
        r1.createCell(2).setCellValue("TRUE");
        r1.createCell(3).setCellValue("FALSE");
        r1.createCell(4).setCellValue("TRUE");
        r1.createCell(5).setCellValue("FALSE");
        r1.createCell(6).setCellValue("120");
        r1.createCell(7).setCellValue("Dr Alice");
        r1.createCell(8).setCellValue("Bob, Carol");
        r1.createCell(9).setCellValue("Dave");
        r1.createCell(10).setCellValue("");
        // Data row 2
        Row r2 = sheet.createRow(2);
        r2.createCell(0).setCellValue("MAT2002");
        r2.createCell(1).setCellValue("Advanced Math");
        r2.createCell(2).setCellValue("FALSE");
        r2.createCell(3).setCellValue("TRUE");
        r2.createCell(4).setCellValue("FALSE");
        r2.createCell(5).setCellValue("TRUE");
        r2.createCell(6).setCellValue("80");
        r2.createCell(7).setCellValue("Prof. Bob");
        r2.createCell(8).setCellValue("");
        r2.createCell(9).setCellValue("Eve, Frank ,  Grace ");
        r2.createCell(10).setCellValue("Heidi");

        // 2) Write workbook to a temp file
        File excel = tempDir.resolve("subjects.xlsx").toFile();
        try (FileOutputStream out = new FileOutputStream(excel)) {
            wb.write(out);
        }
        wb.close();

        // 3) Invoke the service
        List<SubjectPlanInfo> infos = service.readExcelFile(excel.getAbsolutePath());

        // 4) Verify ModuleGenerator calls
        verify(moduleGenerator, times(1)).saveModulesFromExcel("CSC1001", "Intro to CS");
        verify(moduleGenerator, times(1)).saveModulesFromExcel("MAT2002", "Advanced Math");

        // 5) Assertions on returned data
        assertEquals(2, infos.size());

        SubjectPlanInfo info1 = infos.get(0);
        assertEquals("CSC1001", info1.getSubjectCode());
        assertEquals("Intro to CS", info1.getSubjectName());
        assertTrue(info1.hasLecture());
        assertFalse(info1.hasPractical());
        assertTrue(info1.hasTutorial());
        assertFalse(info1.hasWorkshop());
        assertEquals(120, info1.getTotalStudents());
        assertEquals("Dr Alice", info1.getMainLecturer());
        assertIterableEquals(List.of("Bob","Carol"), info1.getPracticalTutor());
        assertIterableEquals(List.of("Dave"),      info1.getTutorialTutor());
        assertTrue(info1.getWorkshopTutor().isEmpty());

        SubjectPlanInfo info2 = infos.get(1);
        assertEquals("MAT2002", info2.getSubjectCode());
        assertEquals("Advanced Math", info2.getSubjectName());
        assertFalse(info2.hasLecture());
        assertTrue(info2.hasPractical());
        assertFalse(info2.hasTutorial());
        assertTrue(info2.hasWorkshop());
        assertEquals(80, info2.getTotalStudents());
        assertEquals("Prof. Bob", info2.getMainLecturer());
        assertTrue(info2.getPracticalTutor().isEmpty());
        assertIterableEquals(List.of("Eve","Frank","Grace"), info2.getTutorialTutor());
        assertIterableEquals(List.of("Heidi"), info2.getWorkshopTutor());
    }
}
