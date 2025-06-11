package com.sunway.course.timetable.util;
import java.util.List;

public class IntakeUtils {

    private static final List<String> NORMALIZED_INTAKES = List.of("January", "April", "August");

    public static String getIntakeLabel(int semester, String userSelectedIntake, int baseYear) {
        String normalized = userSelectedIntake.equalsIgnoreCase("September") ? "August" : userSelectedIntake;
        int baseIndex = NORMALIZED_INTAKES.indexOf(normalized);
        int totalIntakes = NORMALIZED_INTAKES.size();

        int offset = semester - 1;
        int newIndex = (baseIndex - offset % totalIntakes + totalIntakes) % totalIntakes;
        int roundsBack = (offset + (totalIntakes - baseIndex)) / totalIntakes;
        int newYear = baseYear - roundsBack + 1;

        String displayIntake = (normalized.equals("August") && userSelectedIntake.equalsIgnoreCase("September"))
            ? "September"
            : NORMALIZED_INTAKES.get(newIndex);

        return displayIntake + "-" + newYear;
    }
}

