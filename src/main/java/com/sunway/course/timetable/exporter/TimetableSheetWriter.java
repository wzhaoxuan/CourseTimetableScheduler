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

    public Workbook generateCoreWorkbook(String sheetName, List<Session> sessions, Map<Long, String> venueMap, Map<Long, String> moduleMap) {
        processedSessionKeys.clear();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle wrapStyle = createBodyStyle(workbook);
        Map<String, CellStyle> typeColorMap = createTypeColorStyles(workbook);

        Map<LocalTime, Integer> timeSlotColumnMap = generateTimeSlots(sheet, headerStyle);
        int currentRow = 1;


        for (String day : DAYS) {
            // extract sessions for this day
            List<Session> daySessions = sessions.stream()
                .filter(s -> day.equals(s.getDay()))
                .sorted(Comparator.comparing(Session::getStartTime))
                .collect(Collectors.toList());

            List<Row> usedRows = new ArrayList<>();
            List<boolean[]> occupancy = new ArrayList<>();

            for (Session session : daySessions) {
                String venue = venueMap.getOrDefault(session.getId(), "Unknown");
                String moduleCode = moduleMap.getOrDefault(session.getId(), "Unknown");
                String group = session.getTypeGroup().split("-")[2];
                String type = session.getType();
                String lecturer = session.getLecturer() != null ? session.getLecturer().getName() : "Unknown";
                String content = String.format("%s-%s-%s\n(%s)\n%s", moduleCode, group, type, lecturer, venue);

                String sessionKey = session.getType().equalsIgnoreCase("Lecture")
                        ? session.getTypeGroup()                                      // one per group
                        : session.getDay() + "|" + session.getStartTime() + "|" + session.getTypeGroup();
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
            } else {
                Row emptyRow = sheet.createRow(currentRow++);
                Cell dayCell = emptyRow.createCell(0);
                dayCell.setCellValue(day);
                dayCell.setCellStyle(headerStyle);
            }
        }

        for (int i = 0; i <= timeSlotColumnMap.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }

    /** Original method signature preserved, now delegates to core */
    public Workbook generateWorkbook(
            String sheetName,
            Map<String, List<Session>> groupedSessions,
            Map<Long, String> venueMap,
            Map<Long, String> moduleMap
    ) {
        List<Session> sessions = groupedSessions.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        return generateCoreWorkbook(sheetName, sessions, venueMap, moduleMap);
    }

    public Workbook generateWorkbookSimple(String sheetName, List<Session> sessions, 
                                       Map<Long, String> venueMap, Map<Long, String> moduleMap) {

        return generateCoreWorkbook(sheetName, sessions, venueMap, moduleMap);
    }

    public Workbook generateWorkbookFromSessions(String sheetName, List<Session> sessions) {

        Map<Long, String> venueMap = sessions.stream().collect(Collectors.toMap(
            Session::getId,
            s -> venueAssignmentService.getVenueBySessionId(s.getId()).map(Venue::getName).orElse("Unknown"),
            (a,b) -> a
        ));
        Map<Long, String> moduleMap = sessions.stream().collect(Collectors.toMap(
            Session::getId,
            s -> planContentService.getModuleBySessionId(s.getId()).map(pc->pc.getModule().getId()).orElse("Unknown"),
            (a,b) -> a
        ));
        return generateCoreWorkbook(sheetName, sessions, venueMap, moduleMap);
    }

     public Workbook generateWorkbookFromSessions(String sheetName, List<Session> sessions, Map<Long, String> versionVenueMap) {
        Map<Long, String> moduleMap = sessions.stream().collect(Collectors.toMap(
            Session::getId,
            s -> planContentService.getModuleBySessionId(s.getId()).map(pc->pc.getModule().getId()).orElse("Unknown"),
            (a,b) -> a
        ));
        return generateCoreWorkbook(sheetName, sessions, versionVenueMap, moduleMap);
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


    public void addFitnessScore(Workbook workbook, double score) {
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.createRow(sheet.getLastRowNum() + 2);
        Cell scoreCell = row.createCell(1);
        scoreCell.setCellValue(score + "%");

        CellStyle scoreStyle = workbook.createCellStyle();
        scoreStyle.setFillForegroundColor((score < 80.0 ? IndexedColors.RED : IndexedColors.BRIGHT_GREEN).getIndex());
        scoreStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        scoreCell.setCellStyle(scoreStyle);
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

