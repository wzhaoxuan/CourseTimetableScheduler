package com.sunway.course.timetable.util;

import com.sunway.course.timetable.service.WeekDayConstraintServiceImpl;
import org.springframework.stereotype.Component;

@Component
public class LecturerDayAvailabilityUtil {

    private final WeekDayConstraintServiceImpl weekDayConstraintService;

    public LecturerDayAvailabilityUtil(WeekDayConstraintServiceImpl weekDayConstraintService) {
        this.weekDayConstraintService = weekDayConstraintService;
    }

    /**
     * Returns true if the lecturer is unavailable on the given day.
     * Days should be title case (e.g., "Monday", "Tuesday").
     */
    public boolean isUnavailable(Long lecturerId, String day) {
        return weekDayConstraintService.getWeekDayConstraintByLecturerId(lecturerId)
            .map(c -> switch (day.toLowerCase()) {
                case "monday" -> c.isMonday();
                case "tuesday" -> c.isTuesday();
                case "wednesday" -> c.isWednesday();
                case "thursday" -> c.isThursday();
                case "friday" -> c.isFriday();
                default -> true;
            })
            .orElse(false); // If not found, assume available
    }
}
