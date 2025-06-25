package com.sunway.course.timetable.unit.excelReader;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.model.WeekDayConstraint;
import com.sunway.course.timetable.service.LecturerServiceImpl;
import com.sunway.course.timetable.service.WeekDayConstraintServiceImpl;
import com.sunway.course.timetable.service.excelReader.LecturerAvailablityExcelReaderService;

@ExtendWith(MockitoExtension.class)
public class LecturerAvailabilityExcelReaderServiceTest {

    @Mock
    private WeekDayConstraintServiceImpl weekDayConstraintService;

    @Mock
    private LecturerServiceImpl lecturerService;

    @InjectMocks
    private LecturerAvailablityExcelReaderService readerService;

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("readLecturerAvailabilityExcelFile creates new constraints when none exist")
    void testCreatesNewConstraints(@TempDir Path tempDir) throws Exception {
        // 1) Build an in-memory workbook
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        // Header
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("LecturerID");
        header.createCell(1).setCellValue("Monday");
        header.createCell(2).setCellValue("Tuesday");
        header.createCell(3).setCellValue("Wednesday");
        header.createCell(4).setCellValue("Thursday");
        header.createCell(5).setCellValue("Friday");
        // Data row for lecturer 101
        Row r1 = sheet.createRow(1);
        r1.createCell(0).setCellValue("101");
        r1.createCell(1).setCellValue("TRUE");
        r1.createCell(2).setCellValue("FALSE");
        r1.createCell(3).setCellValue("TRUE");
        r1.createCell(4).setCellValue("FALSE");
        r1.createCell(5).setCellValue("TRUE");
        // Data row for lecturer 202
        Row r2 = sheet.createRow(2);
        r2.createCell(0).setCellValue("202");
        r2.createCell(1).setCellValue("FALSE");
        r2.createCell(2).setCellValue("FALSE");
        r2.createCell(3).setCellValue("FALSE");
        r2.createCell(4).setCellValue("TRUE");
        r2.createCell(5).setCellValue("TRUE");

        File file = tempDir.resolve("lec_avail.xlsx").toFile();
        try (FileOutputStream out = new FileOutputStream(file)) {
            wb.write(out);
        }
        wb.close();

        // 2) Stub lecturerService to return a Lecturer for each ID
        Lecturer lec101 = new Lecturer(); lec101.setId(101L);
        Lecturer lec202 = new Lecturer(); lec202.setId(202L);
        when(lecturerService.getLecturerById(101L)).thenReturn(Optional.of(lec101));
        when(lecturerService.getLecturerById(202L)).thenReturn(Optional.of(lec202));

        // Stub constraint service to report none exist
        when(weekDayConstraintService.getWeekDayConstraintByLecturerId(101L))
            .thenReturn(Optional.empty());
        when(weekDayConstraintService.getWeekDayConstraintByLecturerId(202L))
            .thenReturn(Optional.empty());

        // 3) Execute
        List<WeekDayConstraint> constraints =
            readerService.readLecturerAvailabilityExcelFile(file.getAbsolutePath());

        // 4) Capture what was saved
        ArgumentCaptor<WeekDayConstraint> cap = ArgumentCaptor.forClass(WeekDayConstraint.class);
        verify(weekDayConstraintService, times(2)).addWeekDayConstraint(cap.capture());

        List<WeekDayConstraint> saved = cap.getAllValues();
        assertEquals(2, saved.size());

        WeekDayConstraint c1 = saved.get(0);
        assertEquals(lec101, c1.getLecturer());
        assertTrue(c1.isMonday());
        assertFalse(c1.isTuesday());
        assertTrue(c1.isWednesday());
        assertFalse(c1.isThursday());
        assertTrue(c1.isFriday());

        WeekDayConstraint c2 = saved.get(1);
        assertEquals(lec202, c2.getLecturer());
        assertFalse(c2.isMonday());
        assertFalse(c2.isTuesday());
        assertFalse(c2.isWednesday());
        assertTrue(c2.isThursday());
        assertTrue(c2.isFriday());

        // And ensure the returned list matches
        assertEquals(2, constraints.size());
        assertTrue(constraints.containsAll(saved));
    }

    @Test
    @DisplayName("readLecturerAvailabilityExcelFile updates existing constraints")
    void testUpdatesExistingConstraint(@TempDir Path tempDir) throws Exception {
        // Build workbook with a single lecturer 303
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("LecturerID");
        header.createCell(1).setCellValue("Monday");
        header.createCell(2).setCellValue("Tuesday");
        header.createCell(3).setCellValue("Wednesday");
        header.createCell(4).setCellValue("Thursday");
        header.createCell(5).setCellValue("Friday");
        Row r = sheet.createRow(1);
        r.createCell(0).setCellValue("303");
        r.createCell(1).setCellValue("FALSE");
        r.createCell(2).setCellValue("TRUE");
        r.createCell(3).setCellValue("FALSE");
        r.createCell(4).setCellValue("TRUE");
        r.createCell(5).setCellValue("FALSE");

        File file = tempDir.resolve("lec_avail2.xlsx").toFile();
        try (FileOutputStream out = new FileOutputStream(file)) {
            wb.write(out);
        }
        wb.close();

        // Stub services
        Lecturer lec303 = new Lecturer(); lec303.setId(303L);
        when(lecturerService.getLecturerById(303L)).thenReturn(Optional.of(lec303));

        // Provide an existing constraint for 303
        WeekDayConstraint existing = new WeekDayConstraint();
        existing.setLecturer(lec303);
        // initial flags all true
        existing.setMonday(true);
        existing.setTuesday(true);
        existing.setWednesday(true);
        existing.setThursday(true);
        existing.setFriday(true);

        when(weekDayConstraintService.getWeekDayConstraintByLecturerId(303L))
            .thenReturn(Optional.of(existing));

        // Execute
        List<WeekDayConstraint> constraints =
            readerService.readLecturerAvailabilityExcelFile(file.getAbsolutePath());

        // Capture update
        ArgumentCaptor<WeekDayConstraint> cap = ArgumentCaptor.forClass(WeekDayConstraint.class);
        verify(weekDayConstraintService).addWeekDayConstraint(cap.capture());

        WeekDayConstraint updated = cap.getValue();
        assertSame(existing, updated, "Should update the same constraint instance");
        assertFalse(updated.isMonday());
        assertTrue(updated.isTuesday());
        assertFalse(updated.isWednesday());
        assertTrue(updated.isThursday());
        assertFalse(updated.isFriday());

        // Returned list should contain that instance
        assertEquals(1, constraints.size());
        assertSame(existing, constraints.get(0));
    }
}