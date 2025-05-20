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

    public static boolean isWithinThreeMonths(Month baseMonth, Month intakeMonth) {
        if (baseMonth == null || intakeMonth == null) return false;

        int base = baseMonth.getValue();    // 1 to 12
        int intake = intakeMonth.getValue();

        // Calculate both forward and backward difference with wrap-around
        int forwardDiff = Math.floorMod(intake - base, 12);
        int backwardDiff = Math.floorMod(base - intake, 12);

        // Accept within ±3 months
        return forwardDiff > 3 || backwardDiff > 3;
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
