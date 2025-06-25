package com.sunway.course.timetable.unit.excelReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sunway.course.timetable.model.assignment.StudentSem;
import com.sunway.course.timetable.service.excelReader.StudentSemExcelReaderService;

public class StudentSemExcelReaderServiceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("readStudentSemExcelFile groups StudentSem by semester correctly")
    void testReadStudentSemExcelFile(@TempDir Path tempDir) throws Exception {
        // 1) Build an in-memory workbook
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Students");
        // Header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Semester");
        header.createCell(1).setCellValue("StudentID");
        header.createCell(2).setCellValue("Programme");
        // Data row for semester 1
        Row r1 = sheet.createRow(1);
        r1.createCell(0).setCellValue("1");
        r1.createCell(1).setCellValue("1001");
        r1.createCell(2).setCellValue("BCS");
        // Data row for semester 2
        Row r2 = sheet.createRow(2);
        r2.createCell(0).setCellValue("2");
        r2.createCell(1).setCellValue("1002");
        r2.createCell(2).setCellValue("BBA");
        // Another row for semester 1
        Row r3 = sheet.createRow(3);
        r3.createCell(0).setCellValue("1");
        r3.createCell(1).setCellValue("1003");
        r3.createCell(2).setCellValue("BSc");

        // 2) Write workbook to a temp file
        File file = tempDir.resolve("students.xlsx").toFile();
        try (FileOutputStream out = new FileOutputStream(file)) {
            wb.write(out);
        }
        wb.close();

        // 3) Invoke service
        StudentSemExcelReaderService service = new StudentSemExcelReaderService();
        Map<Integer, List<StudentSem>> result = service.readStudentSemExcelFile(file.getAbsolutePath());

        // 4) Assertions
        assertEquals(2, result.size(), "Should have exactly two semesters");

        // Semester 1
        assertTrue(result.containsKey(1), "Semester 1 should be present");
        List<StudentSem> sem1List = result.get(1);
        assertEquals(2, sem1List.size(), "Semester 1 must have 2 entries");
        assertTrue(sem1List.stream().anyMatch(ss -> ss.getStudentId() == 1001 && "BCS".equals(ss.getProgramme())));
        assertTrue(sem1List.stream().anyMatch(ss -> ss.getStudentId() == 1003 && "BSc".equals(ss.getProgramme())));

        // Semester 2
        assertTrue(result.containsKey(2), "Semester 2 should be present");
        List<StudentSem> sem2List = result.get(2);
        assertEquals(1, sem2List.size(), "Semester 2 must have 1 entry");
        StudentSem entry = sem2List.get(0);
        assertEquals(1002, entry.getStudentId());
        assertEquals("BBA", entry.getProgramme());
    }

    @Test
    @DisplayName("readStudentSemExcelFile on missing file returns empty map")
    void testReadStudentSemExcelFile_missingFile() throws FileNotFoundException {
        StudentSemExcelReaderService service = new StudentSemExcelReaderService();
        Map<Integer, List<StudentSem>> result = service.readStudentSemExcelFile("does-not-exist.xlsx");
        assertTrue(result.isEmpty(), "Missing file should yield empty map");
    }
}
