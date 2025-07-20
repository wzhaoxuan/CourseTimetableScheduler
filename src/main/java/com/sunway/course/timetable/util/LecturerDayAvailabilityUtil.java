package com.sunway.course.timetable.util;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.service.WeekDayConstraintServiceImpl;

@Component
public class LecturerDayAvailabilityUtil {

    public static final int MAX_UNAVAILABLE_DAYS = 3;

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

    /**
     * Throws if a lecturer is unavailable on 4 or more weekdays.
     *
     * @param lecturerId    the DB id of the lecturer
     * @param lecturerName  the lecturer’s name (for error text)
     */
    public void validateLecturerWeekdays(
        long lecturerId,
        String lecturerName
    ) {
        List<String> days = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        long unavailableCount = days.stream()
            .filter(d -> isUnavailable(lecturerId, d))
            .count();

        if (unavailableCount > MAX_UNAVAILABLE_DAYS) {
            throw new IllegalStateException(String.format(
                "Lecturer %s is unavailable on %d weekdays (max %d).",
                lecturerName, unavailableCount, MAX_UNAVAILABLE_DAYS
            ));
        }
    }
}
