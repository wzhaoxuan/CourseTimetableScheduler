package com.sunway.course.timetable.util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.service.PlanContentServiceImpl;
import com.sunway.course.timetable.service.venue.VenueAssignmentServiceImpl;

@Component
public class TimetableExcelExporter {

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<String> DAYS = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");

    @Autowired
    private PlanContentServiceImpl planContentService;

    @Autowired
    private VenueAssignmentServiceImpl venueAssignmentService;

    public List<File> exportWithFitnessAnnotation(Map<Integer, Map<String, List<Session>>> sessionBySemesterAndModule, 
                                                    double fitnessScore, String programme, String intake, int year) {
        System.out.printf("Final timetable fitness score: %.2f%%%n", fitnessScore);
        List<File> files = new ArrayList<>();
        for (Map.Entry<Integer, Map<String, List<Session>>> entry : sessionBySemesterAndModule.entrySet()) {
            int semester = entry.getKey();
            if (semester <= 0) continue; 
            List<Session> allSessions = entry.getValue().values().stream().flatMap(List::stream).collect(Collectors.toList());
            File file = exportSemesterTimetable(semester, allSessions, fitnessScore, programme, intake, year);
            files.add(file);
        }

        return files;
    }

    private File exportSemesterTimetable(int semester, List<Session> sessions, double fitnessScore,
                                          String programme, String intake, int year) {
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

        Map<String, List<Session>> groupedSessions = sessions.stream().collect(Collectors.groupingBy(
            s -> String.join("|",
                s.getDay(),
                s.getStartTime().toString(),
                s.getTypeGroup(),
                s.getType(),
                s.getLecturer().getName(),
                String.valueOf(
                    venueAssignmentService.getVenueBySessionId(s.getId())
                        .map(v -> v.getName())
                        .orElse("Unknown")
                        )
        )));

        for (List<Session> groupList : groupedSessions.values()) {
            Session s = groupList.get(0);
            String day = s.getDay();
            Row row = dayRowMap.get(day);
            if (row == null) continue;

            LocalTime start = s.getStartTime();
            LocalTime end = s.getEndTime();
            int firstCol = timeSlotColumnMap.get(start);
            int lastCol = timeSlotColumnMap.get(end.minusMinutes(30));

            String moduleCode = planContentService.getModuleBySessionId(s.getId()).map(p -> p.getModule().getId()).orElse("Unknown");
            String venue = venueAssignmentService.getVenueBySessionId(s.getId()).map(v -> v.getName()).orElse("Unknown");
            String lecturerName = s.getLecturer().getName();
            String group = s.getTypeGroup().split("-")[2];
            String type = s.getType();

            String content = String.format("%s-%s-%s\n(%s)\n%s", moduleCode, group, type, lecturerName, venue);

            Cell cell = row.getCell(firstCol);
            if (cell == null) cell = row.createCell(firstCol);
            String existing = cell.getStringCellValue();
            cell.setCellValue(existing == null || existing.isEmpty() ? content : existing + "\n\n" + content);

            CellStyle colorStyle = typeColorMap.getOrDefault(type.toLowerCase(), wrapStyle);
            cell.setCellStyle(colorStyle);

            if (lastCol > firstCol && !isRegionAlreadyMerged(sheet, row.getRowNum(), firstCol, lastCol)) {
                sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), firstCol, lastCol));
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

        Row row = sheet.createRow(sheet.getLastRowNum() + 2);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue("Timetable Fitness Score:");
        labelCell.setCellStyle(headerStyle);

        Cell scoreCell = row.createCell(1);
        scoreCell.setCellValue(fitnessScore + "%");
        CellStyle scoreStyle = workbook.createCellStyle();
        scoreStyle.cloneStyleFrom(headerStyle);
        scoreStyle.setFillForegroundColor((fitnessScore < 80.0 ? IndexedColors.RED : IndexedColors.BRIGHT_GREEN).getIndex());
        scoreStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        scoreCell.setCellStyle(scoreStyle);

        String userHome = System.getProperty("user.home");
        String intakeLabel = getIntakeLabel(semester, intake, year); // use inputs
        String fileName = String.format("%s-%s S%d.xlsx", programme, intakeLabel, semester);
        File file = new File(userHome + "/Downloads/" + fileName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            workbook.write(out);
            System.out.println("Timetable saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
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

    private static final List<String> INTAKES = List.of("January", "April", "August");

    // Mapping from normalized index → user-chosen label
    private String getDisplayIntake(String normalized, String userSelectedIntake) {
        if (normalized.equals("August")) {
            return userSelectedIntake.equalsIgnoreCase("September") ? "September" : "August";
        }
        return normalized;
    }

    private String getIntakeLabel(int semester, String userSelectedIntake, int baseYear) {
        // Normalize logic
        String baseNormalized = userSelectedIntake.equalsIgnoreCase("September") ? "August" : userSelectedIntake;
        int baseIndex = INTAKES.indexOf(baseNormalized);
        int offset = semester - 1;

        int newIndex = (baseIndex - offset % 3 + 3) % 3;
        int yearOffset = (baseIndex - offset < 0) ? (offset - baseIndex + 2) / 3 : -(offset / 3);

        String normalizedIntake = INTAKES.get(newIndex);
        String displayIntake = getDisplayIntake(normalizedIntake, userSelectedIntake);

        int year = baseYear + yearOffset;
        return displayIntake + "-" + year;
    }


}



