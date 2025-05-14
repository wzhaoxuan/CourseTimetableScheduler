package com.sunway.course.timetable.util;
import org.apache.poi.ss.usermodel.Cell;

public class ExcelUtil {

    public static String getCellValue(Cell cell){
        if(cell == null) return "";
        return switch (cell.getCellType()){
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    public static boolean parseBoolean(String value){
        if(value == null || value.isEmpty()) return false;
        return "true".equalsIgnoreCase(value.trim()) || "yes".equalsIgnoreCase(value.trim());
        }

    public static int parseInt(String value){
        if(value == null || value.isEmpty()) return 0;
        try{
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e){
            return 0;
        }
    }
}
