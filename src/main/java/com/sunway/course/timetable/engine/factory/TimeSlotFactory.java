package com.sunway.course.timetable.engine.factory;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.sunway.course.timetable.engine.TimeSlot;

public class TimeSlotFactory {
    public static List<TimeSlot> generateValidTimeSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        
        // Constraints:
        // - Days: Monday to Friday
        // - Time: 08:00 to 18:00
        // - Assume sessions are 2-hour blocks (customizable)
        int durationsInHours = 2;

        for (DayOfWeek day : DayOfWeek.values()) {
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) continue;

            LocalTime start = LocalTime.of(8, 0);
            LocalTime lastestStart = LocalTime.of(18 - durationsInHours, 0); // end by 18:00

            while(!start.isAfter(lastestStart)) {
                LocalTime end = start.plusHours(durationsInHours);
                slots.add(new TimeSlot(day, start, end));
                start = start.plusMinutes(60); // 1-hour interval between options
            }
        }
        return slots;
    }
}
