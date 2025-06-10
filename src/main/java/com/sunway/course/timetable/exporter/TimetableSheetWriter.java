package com.sunway.course.timetable.exporter;

import com.sunway.course.timetable.model.Session;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TimetableSheetWriter {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<String> DAYS = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");

    private final Workbook workbook;
    private final Sheet sheet;
    private final CellStyle headerStyle;
    private final CellStyle wrapStyle;
    private final Map<String, CellStyle> typeColorMap;
    private final Map<LocalTime, Integer> timeSlotColumnMap = new HashMap<>();
    private final Map<String, Row> dayRowMap = new HashMap<>();

    public TimetableSheetWriter(String sheetName) {
        this.workbook = new XSSFWorkbook();
        this.sheet = workbook.createSheet(sheetName);
        this.headerStyle = createHeaderStyle(workbook);
        this.wrapStyle = createBodyStyle(workbook);
        this.typeColorMap = createTypeColorStyles(workbook);
        initializeHeader();
    }

    private void initializeHeader() {
        TreeSet<LocalTime> timeSlots = new TreeSet<>();
        LocalTime time = LocalTime.of(8, 0);
        while (!time.isAfter(LocalTime.of(18, 0))) {
            timeSlots.add(time);
            time = time.plusMinutes(30);
        }

        Row headerRow = sheet.createRow(0);
        Cell dayCell = headerRow.createCell(0);
        dayCell.setCellValue("Day / Time");
        dayCell.setCellStyle(headerStyle);

        int colIndex = 1;
        for (LocalTime slot : timeSlots) {
            Cell cell = headerRow.createCell(colIndex);
            cell.setCellValue(slot.format(TIME_FORMATTER));
            cell.setCellStyle(headerStyle);
            timeSlotColumnMap.put(slot, colIndex);
            colIndex++;
        }

        int rowIndex = 1;
        for (String day : DAYS) {
            Row row = sheet.createRow(rowIndex++);
            Cell cell = row.createCell(0);
            cell.setCellValue(day);
            cell.setCellStyle(headerStyle);
            dayRowMap.put(day, row);
        }
    }

    public void writeSessions(Map<String, List<Session>> groupedSessions, Map<Long, String> venueMap, Map<Long, String> moduleMap) {
        for (List<Session> groupList : groupedSessions.values()) {
            Session s = groupList.get(0);
            if (s == null) continue;

            Row row = dayRowMap.get(s.getDay());
            if (row == null) continue;

            LocalTime start = s.getStartTime();
            LocalTime end = s.getEndTime();
            int firstCol = timeSlotColumnMap.getOrDefault(start, -1);
            int lastCol = timeSlotColumnMap.getOrDefault(end.minusMinutes(30), -1);

            if (firstCol == -1 || lastCol == -1) continue;

            String moduleCode = moduleMap.getOrDefault(s.getId(), "Unknown");
            String venue = venueMap.getOrDefault(s.getId(), "Unknown");
            String lecturer = s.getLecturer() != null ? s.getLecturer().getName() : "Unknown";
            String group = s.getTypeGroup().split("-")[2];
            String type = s.getType();

            String content = String.format("%s-%s-%s\n(%s)\n%s", moduleCode, group, type, lecturer, venue);

            Cell cell = row.getCell(firstCol);
            if (cell == null) cell = row.createCell(firstCol);
            String existing = cell.getStringCellValue();
            cell.setCellValue(existing == null || existing.isEmpty() ? content : existing + "\n\n" + content);

            CellStyle colorStyle = typeColorMap.getOrDefault(type.toLowerCase(), wrapStyle);
            cell.setCellStyle(colorStyle);

            if (lastCol > firstCol && !isMerged(sheet, row.getRowNum(), firstCol, lastCol)) {
                sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), firstCol, lastCol));
            }
        }
    }

    public File exportToFile(String filename) throws IOException {
        for (int i = 0; i <= timeSlotColumnMap.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        for (Row row : dayRowMap.values()) {
            float maxHeight = sheet.getDefaultRowHeightInPoints();
            for (int j = 1; j <= timeSlotColumnMap.size(); j++) {
                Cell cell = row.getCell(j);
                if (cell != null && cell.getStringCellValue() != null) {
                    int lines = cell.getStringCellValue().split("\\n").length;
                    maxHeight = Math.max(maxHeight, lines * sheet.getDefaultRowHeightInPoints());
                }
            }
            row.setHeightInPoints(maxHeight);
        }

        File file = new File(System.getProperty("user.home") + "/Downloads/" + filename);
        try (FileOutputStream out = new FileOutputStream(file)) {
            workbook.write(out);
        }
        return file;
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

    private Map<String, CellStyle> createTypeColorStyles(Workbook workbook) {
        Map<String, CellStyle> map = new HashMap<>();
        map.put("lecture", createColoredStyle(workbook, IndexedColors.LIGHT_YELLOW));
        map.put("tutorial", createColoredStyle(workbook, IndexedColors.LIGHT_GREEN));
        map.put("practical", createColoredStyle(workbook, IndexedColors.LIGHT_CORNFLOWER_BLUE));
        return map;
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

    private boolean isMerged(Sheet sheet, int row, int start, int end) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            if (range.getFirstRow() == row && range.getLastRow() == row &&
                range.getFirstColumn() == start && range.getLastColumn() == end) {
                return true;
            }
        }
        return false;
    }

    public void addFitnessScore(double score) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 2);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue("Timetable Fitness Score:");
        labelCell.setCellStyle(headerStyle);

        Cell scoreCell = row.createCell(1);
        scoreCell.setCellValue(score + "%");

        CellStyle scoreStyle = workbook.createCellStyle();
        scoreStyle.cloneStyleFrom(headerStyle);
        scoreStyle.setFillForegroundColor((score < 80.0 ? IndexedColors.RED : IndexedColors.BRIGHT_GREEN).getIndex());
        scoreStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        scoreCell.setCellStyle(scoreStyle);
    }

}

