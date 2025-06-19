package com.sunway.course.timetable.exporter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.service.PlanContentServiceImpl;
import com.sunway.course.timetable.service.venue.VenueAssignmentServiceImpl;

@Component
public class TimetableSheetWriter {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<String> DAYS = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");

    private final Set<String> processedSessionKeys = new HashSet<>();


    @Autowired private VenueAssignmentServiceImpl venueAssignmentService;
    @Autowired private PlanContentServiceImpl planContentService;

    public Workbook generateWorkbook(String sheetName, Map<String, List<Session>> groupedSessions, Map<Long, String> venueMap, Map<Long, String> moduleMap) {
        processedSessionKeys.clear();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle wrapStyle = createBodyStyle(workbook);
        Map<String, CellStyle> typeColorMap = createTypeColorStyles(workbook);

        Map<LocalTime, Integer> timeSlotColumnMap = generateTimeSlots(sheet, headerStyle);
        int currentRow = 1;

        for (String day : DAYS) {
            List<Session> daySessions = groupedSessions.values().stream()
                .flatMap(List::stream)
                .filter(s -> s.getDay().equals(day))
                .sorted(Comparator.comparing(Session::getStartTime))
                .toList();

            List<Row> usedRows = new ArrayList<>();
            List<boolean[]> occupancy = new ArrayList<>();

            for (Session session : daySessions) {
                String venue = venueMap.getOrDefault(session.getId(), "Unknown");
                String moduleCode = moduleMap.getOrDefault(session.getId(), "Unknown");
                String group = session.getTypeGroup().split("-")[2];
                String type = session.getType();
                String lecturer = session.getLecturer() != null ? session.getLecturer().getName() : "Unknown";
                String content = String.format("%s-%s-%s\n(%s)\n%s", moduleCode, group, type, lecturer, venue);

                String sessionKey = content + session.getDay() + session.getStartTime();
                if (!processedSessionKeys.add(sessionKey)) continue;

                LocalTime start = session.getStartTime();
                LocalTime end = session.getEndTime();
                int firstCol = timeSlotColumnMap.getOrDefault(start, -1);
                int lastCol = timeSlotColumnMap.getOrDefault(end.minusMinutes(30), -1);
                if (firstCol == -1 || lastCol == -1) continue;

                int targetRowIdx = -1;
                for (int i = 0; i < occupancy.size(); i++) {
                    boolean[] rowOcc = occupancy.get(i);
                    boolean canFit = true;
                    for (int c = firstCol; c <= lastCol; c++) {
                        if (rowOcc[c]) {
                            canFit = false;
                            break;
                        }
                    }
                    if (canFit) {
                        targetRowIdx = i;
                        break;
                    }
                }

                if (targetRowIdx == -1) {
                    Row row = sheet.createRow(currentRow++);
                    usedRows.add(row);
                    occupancy.add(new boolean[21]);
                    targetRowIdx = usedRows.size() - 1;
                }

                Row targetRow = usedRows.get(targetRowIdx);
                boolean[] occRow = occupancy.get(targetRowIdx);
                for (int c = firstCol; c <= lastCol; c++) occRow[c] = true;

                Cell cell = targetRow.createCell(firstCol);
                cell.setCellValue(content);
                cell.setCellStyle(typeColorMap.getOrDefault(type.toLowerCase(), wrapStyle));
                if (lastCol > firstCol) {
                    sheet.addMergedRegion(new CellRangeAddress(targetRow.getRowNum(), targetRow.getRowNum(), firstCol, lastCol));
                }
            }

            if (!usedRows.isEmpty()) {
                int firstRow = usedRows.get(0).getRowNum();
                int lastRow = usedRows.get(usedRows.size() - 1).getRowNum();
                if (firstRow != lastRow) {
                    sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, 0, 0));
                }
                Row labelRow = usedRows.get(0);
                Cell dayCell = labelRow.createCell(0);
                dayCell.setCellValue(day);
                dayCell.setCellStyle(headerStyle);

                for (Row row : usedRows) {
                    float maxHeight = 16f;
                    for (int j = 1; j <= timeSlotColumnMap.size(); j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null && cell.getStringCellValue() != null) {
                            int lines = cell.getStringCellValue().split("\\n").length;
                            maxHeight = Math.max(maxHeight, lines * 16f * 1.3f);
                        }
                    }
                    row.setHeightInPoints(maxHeight);
                }
            }
        }

        for (int i = 0; i <= timeSlotColumnMap.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }

    public Workbook generateWorkbookSimple(String sheetName, List<Session> sessions, 
                                       Map<Long, String> venueMap, Map<Long, String> moduleMap) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle wrapStyle = createBodyStyle(workbook);
        Map<String, CellStyle> typeColorMap = createTypeColorStyles(workbook);

        Map<LocalTime, Integer> timeSlotColumnMap = generateTimeSlots(sheet, headerStyle);
        Map<String, Row> dayRowMap = generateDayRows(sheet, headerStyle);

        for (Session s : sessions) {
            String venue = venueMap.getOrDefault(s.getId(), "Unknown");
            String moduleCode = moduleMap.getOrDefault(s.getId(), "Unknown");

            writeSingleSession(s, venue, moduleCode, sheet, dayRowMap, timeSlotColumnMap, wrapStyle, typeColorMap);
        }

        autoSize(sheet, timeSlotColumnMap, dayRowMap);
        return workbook;
    }

    public Workbook generateWorkbookFromSessions(String sheetName, List<Session> sessions) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);
        
        // Same header generation logic
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle wrapStyle = createBodyStyle(workbook);
        Map<String, CellStyle> typeColorMap = createTypeColorStyles(workbook);

        Map<LocalTime, Integer> timeSlotColumnMap = generateTimeSlots(sheet, headerStyle);
        Map<String, Row> dayRowMap = generateDayRows(sheet, headerStyle);

        for (Session s : sessions) {
            Long sessionId = s.getId();
            String venue = venueAssignmentService.getVenueBySessionId(sessionId)
                                .map(Venue::getName)
                                .orElse("Unknown");

            String moduleCode = planContentService.getModuleBySessionId(sessionId)
                                .map(pc -> pc.getModule().getId())
                                .orElse("Unknown");
            writeSingleSession(s, venue, moduleCode, sheet, dayRowMap, timeSlotColumnMap, wrapStyle, typeColorMap);
        }

        autoSize(sheet, timeSlotColumnMap, dayRowMap);
        return workbook;
    }

    private Map<LocalTime, Integer> generateTimeSlots(Sheet sheet, CellStyle headerStyle) {
        Map<LocalTime, Integer> map = new HashMap<>();
        Row headerRow = sheet.createRow(0);
        Cell title = headerRow.createCell(0);
        title.setCellValue("Day / Time");
        title.setCellStyle(headerStyle);

        int col = 1;
        for (LocalTime t = LocalTime.of(8, 0); !t.isAfter(LocalTime.of(18, 0)); t = t.plusMinutes(30)) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(t.format(TIME_FORMATTER));
            cell.setCellStyle(headerStyle);
            map.put(t, col++);
        }
        return map;
    }

    private Map<String, Row> generateDayRows(Sheet sheet, CellStyle headerStyle) {
        Map<String, Row> map = new HashMap<>();
        int rowIndex = 1;
        for (String day : DAYS) {
            Row row = sheet.createRow(rowIndex++);
            Cell cell = row.createCell(0);
            cell.setCellValue(day);
            cell.setCellStyle(headerStyle);
            map.put(day, row);
        }
        return map;
    }

    private void writeSingleSession(Session s, String venue, String moduleCode, Sheet sheet,
        Map<String, Row> dayRowMap, Map<LocalTime, Integer> timeSlotColumnMap,
        CellStyle wrapStyle, Map<String, CellStyle> typeColorMap) {

        Row row = dayRowMap.get(s.getDay());
        if (row == null) return;

        LocalTime start = s.getStartTime();
        LocalTime end = s.getEndTime();
        int firstCol = timeSlotColumnMap.getOrDefault(start, -1);
        int lastCol = timeSlotColumnMap.getOrDefault(end.minusMinutes(30), -1);
        if (firstCol == -1 || lastCol == -1) return;

        String group = s.getTypeGroup().split("-")[2];
        String type = s.getType();
        String lecturer = s.getLecturer() != null ? s.getLecturer().getName() : "Unknown";
        String content = String.format("%s-%s-%s\n(%s)\n%s", moduleCode, group, type, lecturer, venue);

        Cell cell = row.getCell(firstCol);
        if (cell == null) {
            cell = row.createCell(firstCol);
            cell.setCellValue(content);
        } else {
            String existing = cell.getStringCellValue();
            cell.setCellValue(existing + "\n\n\n" + content); 
        }

        cell.setCellStyle(typeColorMap.getOrDefault(type.toLowerCase(), wrapStyle));

        if (lastCol > firstCol && !isMerged(sheet, row.getRowNum(), firstCol, lastCol)) {
            sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), firstCol, lastCol));
        }
    }


    private void autoSize(Sheet sheet, Map<LocalTime, Integer> timeSlotColumnMap, Map<String, Row> dayRowMap) {
        for (int i = 0; i <= timeSlotColumnMap.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        final float baseHeight = 16f;  // Base font height (slightly larger than default 15)
        final float lineSpacing = 1.3f; // Scaling factor to allow for wrapped lines

        for (Row row : dayRowMap.values()) {
            float maxHeight = baseHeight;
            for (int j = 1; j <= timeSlotColumnMap.size(); j++) {
                Cell cell = row.getCell(j);
                if (cell != null && cell.getStringCellValue() != null) {
                    int lines = cell.getStringCellValue().split("\\n").length;
                    maxHeight = Math.max(maxHeight, lines * baseHeight * lineSpacing);
                }
            }
            row.setHeightInPoints(maxHeight);
        }
    }


    public void addFitnessScore(Workbook workbook, double score) {
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.createRow(sheet.getLastRowNum() + 2);
        row.createCell(0).setCellValue("Timetable Fitness Score:");

        Cell scoreCell = row.createCell(1);
        scoreCell.setCellValue(score + "%");

        CellStyle scoreStyle = workbook.createCellStyle();
        scoreStyle.setFillForegroundColor((score < 80.0 ? IndexedColors.RED : IndexedColors.BRIGHT_GREEN).getIndex());
        scoreStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        scoreCell.setCellStyle(scoreStyle);
    }

    private boolean isMerged(Sheet sheet, int row, int start, int end) {
        // Avoid overlapping merged regions
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.getFirstRow() == row && region.getLastRow() == row) {
                if (!(end < region.getFirstColumn() || start > region.getLastColumn())) {
                    return true;
                }
            }
        }
        return false;
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
}

