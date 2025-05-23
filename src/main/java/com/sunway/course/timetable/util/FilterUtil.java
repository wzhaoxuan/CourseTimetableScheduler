package com.sunway.course.timetable.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import com.sunway.course.timetable.config.TitleConfig;

public class FilterUtil {

    public static String extractName(String fullName){
        if(fullName == null || fullName.isEmpty()) return "";

        String normalized  = fullName.replaceAll("[.,]", "").trim();
        String[] parts = normalized.split("\\s+");

        List<String> filteredParts = new ArrayList<>();

        for(String part: parts){
            boolean isTitle = false;
            for(String title: TitleConfig.TITLES){
                if(part.equalsIgnoreCase(title.toUpperCase())){
                    isTitle = true;
                    break;
                }
            }

            if(!isTitle){
                filteredParts.add(part);
            }
        }

        return String.join(" ", filteredParts);
    }

}
