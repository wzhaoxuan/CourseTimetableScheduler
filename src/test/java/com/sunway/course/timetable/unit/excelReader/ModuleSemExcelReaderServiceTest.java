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

import com.sunway.course.timetable.model.assignment.ModuleSem;
import com.sunway.course.timetable.service.excelReader.ModuleSemExcelReaderService;


public class ModuleSemExcelReaderServiceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("readModuleSemExcelFile groups ModuleSem by semester correctly")
    void testReadModuleSemExcelFile() throws Exception {
        // 1) Build an in-memory workbook
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Semesters");
        // Header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Semester");
        header.createCell(1).setCellValue("ModuleID");
        // Data row for semester 1
        Row r1 = sheet.createRow(1);
        r1.createCell(0).setCellValue("1");
        r1.createCell(1).setCellValue("MOD100");
        // Data row for semester 2
        Row r2 = sheet.createRow(2);
        r2.createCell(0).setCellValue("2");
        r2.createCell(1).setCellValue("MOD200");
        // Another row for semester 1
        Row r3 = sheet.createRow(3);
        r3.createCell(0).setCellValue("1");
        r3.createCell(1).setCellValue("MOD101");

        // 2) Write workbook to a temp file
        File file = tempDir.resolve("moduleSem.xlsx").toFile();
        try (FileOutputStream out = new FileOutputStream(file)) {
            wb.write(out);
        }
        wb.close();

        // 3) Invoke service
        ModuleSemExcelReaderService service = new ModuleSemExcelReaderService();
        Map<Integer, List<ModuleSem>> result = service.readModuleSemExcelFile(file.getAbsolutePath());

        // 4) Assertions
        assertEquals(2, result.size(), "Should have exactly two semesters");
        assertTrue(result.containsKey(1), "Semester 1 should be present");
        assertTrue(result.containsKey(2), "Semester 2 should be present");

        List<ModuleSem> sem1List = result.get(1);
        assertEquals(2, sem1List.size(), "Semester 1 must have 2 entries");
        assertTrue(sem1List.stream().anyMatch(ms -> ms.getModuleId().equals("MOD100")));
        assertTrue(sem1List.stream().anyMatch(ms -> ms.getModuleId().equals("MOD101")));

        List<ModuleSem> sem2List = result.get(2);
        assertEquals(1, sem2List.size(), "Semester 2 must have 1 entry");
        assertEquals("MOD200", sem2List.get(0).getModuleId());
    }

    @Test
    @DisplayName("readModuleSemExcelFile on missing file returns empty map")
    void testReadModuleSemExcelFile_missingFile() throws FileNotFoundException {
        ModuleSemExcelReaderService service = new ModuleSemExcelReaderService();
        Map<Integer, List<ModuleSem>> result = service.readModuleSemExcelFile("nonexistent.xlsx");
        assertTrue(result.isEmpty(), "Missing or unreadable file should yield empty map");
    }
}
