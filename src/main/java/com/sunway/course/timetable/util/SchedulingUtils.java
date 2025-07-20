package com.sunway.course.timetable.util;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;

public final class SchedulingUtils {
    private SchedulingUtils() {} // no-op

    /** global max hours per lecturer per run */
    public static final int MAX_TEACHING_HOURS = 20;
    public static final int MAX_UNAVAILABLE_DAYS = 3;

    /**
     * Fail fast if your student count exceeds the largest venue capacity.
     * @param moduleDataList  list of module assignment data, each with eligible students
     * @param studentSemesterMap  map from student ID to their semester (1,
     * @param allVenues  list of all venues, each with a capacity
     * @throws IllegalStateException if no venue can hold all students
     */
    public static void validateSemesterCapacity(
        List<ModuleAssignmentData> moduleDataList,
        Map<Long,Integer> studentSemesterMap,
        List<Venue> allVenues) {

        // build a map <semester → set of distinct student IDs>
        Map<Integer, Set<Long>> studentsPerSem = new HashMap<>();
        for (ModuleAssignmentData data : moduleDataList) {
            for (Student s : data.getEligibleStudents()) {
                Integer sem = studentSemesterMap.get(s.getId());
                if (sem != null) {
                    studentsPerSem
                    .computeIfAbsent(sem, k -> new HashSet<>())
                    .add(s.getId());
                }
            }
        }

        // pull your largest room capacity
        int maxCapacity = allVenues.stream()
            .mapToInt(Venue::getCapacity)
            .max()
            .orElse(0);

        // for each semester, fail if too many
        for (var e : studentsPerSem.entrySet()) {
            int total = e.getValue().size();
            if (total > maxCapacity) {
                throw new IllegalStateException(String.format(
                    "There are %d students but largest venue holds only %d",
                    total, maxCapacity
                ));
            }
        }
    }


    /**
     * Record the next block of hours for a lecturer, and fail if they'd exceed the max.
     * @param lecturerHours  a map from lecturerName → hours already taught
     * @param lecturerName   which lecturer
     * @param hoursToAdd     how many hours this new session will add
     * @throws IllegalStateException if the new total exceeds MAX_TEACHING_HOURS
     */
    public static void recordTeachingHours(Map<String,Integer> lecturerHours,
                                            String lecturerName,
                                            int hoursToAdd) {
        int soFar = lecturerHours.getOrDefault(lecturerName, 0);
        int updated = soFar + hoursToAdd;
        if (updated > MAX_TEACHING_HOURS) {
            throw new IllegalStateException(
                "Lecturer " + lecturerName +
                " is exceeded max teaching hours of " + MAX_TEACHING_HOURS
            );
        }
        lecturerHours.put(lecturerName, updated);
    }

    public static void resetTeachingHours(Map<String, Integer> lecturerHours) {
        lecturerHours.clear();
    }

    /**
     * Throws if a lecturer is unavailable on 4 or more weekdays.
     *
     * @param util          your LecturerDayAvailabilityUtil
     * @param lecturerId    the DB id of the lecturer
     * @param lecturerName  the lecturer’s name (for error text)
     */
    public static void validateLecturerWeekdays(
        LecturerDayAvailabilityUtil util,
        long lecturerId,
        String lecturerName
    ) {
        List<String> days = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        long unavailableCount = days.stream()
            .filter(d -> util.isUnavailable(lecturerId, d))
            .count();

        if (unavailableCount > MAX_UNAVAILABLE_DAYS) {
            throw new IllegalStateException(String.format(
                "Lecturer %s is unavailable on %d weekdays (max %d).",
                lecturerName, unavailableCount, MAX_UNAVAILABLE_DAYS
            ));
        }
    }
}
