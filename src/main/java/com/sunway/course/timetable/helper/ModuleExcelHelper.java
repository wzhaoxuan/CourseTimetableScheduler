package com.sunway.course.timetable.helper;

public class ModuleExcelHelper {

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
