package com.sunway.course.timetable.util;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class DateUtils {

    public static List<String> getMonths(){
        List<String> monthOptions = new ArrayList<String>();

        String[] months = {
                "January", "February", "March", "April",
                "May", "June", "July", "August",
                "September", "October", "November", "December"
        };
        
        for (String month : months) {
            monthOptions.add(month);
        }
        return monthOptions;
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
