package com.sunway.course.timetable.util;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class DateUtils {

    public static String getMonths(){
        String[] months = {
                "January", "February", "March", "April",
                "May", "June", "July", "August",
                "September", "October", "November", "December"
        };
        StringBuilder monthString = new StringBuilder();
        for (String month : months) {
            monthString.append(month).append(", ");
        }
        return monthString.substring(0, monthString.length() - 2); // Remove the last comma and space
    }

    public static int getCurrentYear() {
        return Year.now().getValue();
    }

    public static List<String> getYearOptions() {
        int currentYear = getCurrentYear();
        List<String> yearOptions = new ArrayList<>();
        yearOptions.add(String.valueOf(currentYear));
        return yearOptions;
    }
}
