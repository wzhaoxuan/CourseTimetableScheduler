package com.sunway.course.timetable.util;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class DateUtil {

    public static List<String> getMonths(){
        List<String> monthOptions = new ArrayList<String>();

        String[] months = {
                "January", "February", "March", "April",
                "May", "June", "July", "August",
                "September", "October", "November", "December"
        };
        
        for (String month : months) {
            monthOptions.add(month.toUpperCase());
        }
        return monthOptions;
    }

    public static Month parseMonth(String intakeMonth){
        if (intakeMonth == null || intakeMonth.trim().isEmpty()) return null;

        // Normalize and split the intake string
        String[] parts = intakeMonth.trim().split("\\s+");
        String monthPart = parts[0].toUpperCase();

        try {
            return Month.valueOf(monthPart);
        } catch (IllegalArgumentException e) {
            return null; // Invalid month
        }
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
