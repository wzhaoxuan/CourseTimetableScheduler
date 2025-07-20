package com.sunway.course.timetable.util;
import java.util.List;

public class IntakeUtils {

    private static final List<String> INTAKES = List.of("January", "April", "September");

    public static String getIntakeLabel(int semester, String userSelectedIntake, int baseYear) {

        String capitalizedIntake = capitalize(userSelectedIntake.trim());
        int baseIndex = INTAKES.indexOf(capitalizedIntake);
        if (baseIndex == -1) {
            throw new IllegalArgumentException("Invalid intake: " + userSelectedIntake);
        }

        // calculate how many steps to move backward
        int stepsBack = semester - 1;

        // calculate new intake index (circular modulo)
        int newIndex = Math.floorMod(baseIndex - stepsBack + INTAKES.size(), INTAKES.size());

        // int newIndex = (baseIndex - stepsBack + INTAKES.size()) % INTAKES.size();

        // calculate how many full cycles moved backward
        int fullCycles = (baseIndex - stepsBack < 0)
                ? (Math.abs(baseIndex - stepsBack) + INTAKES.size() - 1) / INTAKES.size()
                : 0;

        int newYear = baseYear - fullCycles;

        String newIntake = INTAKES.get(newIndex);
        return newIntake + "-" + newYear;
    }

     private static String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}


