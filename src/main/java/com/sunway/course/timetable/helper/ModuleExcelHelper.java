package com.sunway.course.timetable.helper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public static List<String> splitSubjectCode(String moduleCode){
        if(moduleCode == null || moduleCode.isEmpty()) return Collections.emptyList();
        
        return Arrays.stream(moduleCode.split("/"))
                    .map(String::trim)
                    .filter(code -> !code.isEmpty())
                    .collect(Collectors.toList());
    }
}
