package com.sunway.course.timetable.util;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.plancontent.PlanContent;
import com.sunway.course.timetable.model.venueAssignment.VenueAssignment;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.sunway.course.timetable.service.venue.VenueAssignmentServiceImpl;
import com.sunway.course.timetable.service.PlanContentServiceImpl;

@Component
public class TimetableExcelExporter {

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<String> DAYS = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");

    @Autowired
    private PlanContentServiceImpl planContentService;

    @Autowired
    private VenueAssignmentServiceImpl venueAssignmentService;

    public void exportTimetableBySemester(Map<Integer, Map<String, List<Session>>> sessionBySemesterAndModule) {
        for (Map.Entry<Integer, Map<String, List<Session>>> entry : sessionBySemesterAndModule.entrySet()) {
            int semester = entry.getKey();
            List<Session> allSessions = new ArrayList<>();

            for (List<Session> sessions : entry.getValue().values()) {
                allSessions.addAll(sessions);
            }

            exportSemesterTimetable(semester, allSessions);
        }
    }

    private void exportSemesterTimetable(int semester, List<Session> sessions) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Semester " + semester);

        TreeSet<LocalTime> timeSlots = new TreeSet<>();
        LocalTime time = LocalTime.of(8, 0);
        while (!time.isAfter(LocalTime.of(18, 0))) {
            timeSlots.add(time);
            time = time.plusMinutes(30);
        }

        Map<LocalTime, Integer> timeSlotColumnMap = new HashMap<>();
        int colIndex = 1;
        Row headerRow = sheet.createRow(0);

        CellStyle headerStyle = createHeaderStyle(workbook);
        Cell dayCell = headerRow.createCell(0);
        dayCell.setCellValue("Day / Time");
        dayCell.setCellStyle(headerStyle);

        for (LocalTime t : timeSlots) {
            Cell cell = headerRow.createCell(colIndex);
            cell.setCellValue(t.format(timeFormatter));
            cell.setCellStyle(headerStyle);
            timeSlotColumnMap.put(t, colIndex);
            colIndex++;
        }

        Map<String, Row> dayRowMap = new HashMap<>();
        int rowIndex = 1;
        CellStyle dayStyle = createHeaderStyle(workbook);
        for (String day : DAYS) {
            Row row = sheet.createRow(rowIndex++);
            Cell cell = row.createCell(0);
            cell.setCellValue(day);
            cell.setCellStyle(dayStyle);
            dayRowMap.put(day, row);
        }

        // Styles
        CellStyle wrapStyle = createBodyStyle(workbook);
        Map<String, CellStyle> typeColorMap = new HashMap<>();
        typeColorMap.put("lecture", createColoredStyle(workbook, IndexedColors.LIGHT_YELLOW));
        typeColorMap.put("tutorial", createColoredStyle(workbook, IndexedColors.LIGHT_GREEN));
        typeColorMap.put("practical", createColoredStyle(workbook, IndexedColors.LIGHT_CORNFLOWER_BLUE));

        for (Session session : sessions) {
            String day = session.getDay();
            Row row = dayRowMap.get(day);
            if (row == null) continue;

            Optional<PlanContent> planOpt = planContentService.getModuleBySession(session);
            String moduleCode = planOpt.map(p -> p.getModule().getId()).orElse("Unknown");

            Optional<VenueAssignment> venueOpt = venueAssignmentService.getVenueBySession(session);
            String venue = venueOpt.map(v -> v.getVenue().getName()).orElse("Unknown");

            LocalTime start = session.getStartTime();
            LocalTime end = session.getEndTime();
            int firstCol = timeSlotColumnMap.get(start);
            int lastCol = timeSlotColumnMap.get(end.minusMinutes(30));

            String type = session.getType();
            String typeGroup = session.getTypeGroup().split("-")[2];
            String lecturerName = session.getLecturer().getName();
            String content = String.format("%s-%s-%s\n(%s)\n%s",
                    moduleCode, typeGroup, type, lecturerName, venue);

            Cell cell = row.createCell(firstCol);
            cell.setCellValue(content);

            CellStyle colorStyle = typeColorMap.getOrDefault(type.toLowerCase(), wrapStyle);
            cell.setCellStyle(colorStyle);

            if (lastCol > firstCol && !isRegionAlreadyMerged(sheet, row.getRowNum(), firstCol, lastCol)) {
                    sheet.addMergedRegion(new CellRangeAddress(
                    row.getRowNum(), row.getRowNum(), firstCol, lastCol));
            }

        }

        // Auto-size
        for (int i = 0; i <= timeSlotColumnMap.size(); i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Fit content height for each cell by estimating tallest cell
        for (int i = 1; i <= DAYS.size(); i++) {
            Row row = sheet.getRow(i);
            float maxHeight = 1;
            for (int j = 1; j <= timeSlotColumnMap.size(); j++) {
                Cell cell = row.getCell(j);
                if (cell != null && cell.getStringCellValue() != null) {
                    int lines = cell.getStringCellValue().split("\\n").length;
                    maxHeight = Math.max(maxHeight, lines * sheet.getDefaultRowHeightInPoints());
                }
            }
            row.setHeightInPoints(maxHeight);
        }

        String userHome = System.getProperty("user.home");
        String downloadPath = userHome + "/Downloads/Semester_" + semester + "_Timetable.xlsx";
        try (FileOutputStream out = new FileOutputStream(downloadPath)) {
            workbook.write(out);
            System.out.println("Timetable saved to: " + downloadPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CellStyle createBodyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createColoredStyle(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private boolean isRegionAlreadyMerged(Sheet sheet, int rowIndex, int firstCol, int lastCol) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            if (range.getFirstRow() == rowIndex &&
                range.getLastRow() == rowIndex &&
                range.getFirstColumn() == firstCol &&
                range.getLastColumn() == lastCol) {
                return true;
            }
        }
        return false;
    }

}



