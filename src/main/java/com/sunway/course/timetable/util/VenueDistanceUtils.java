package com.sunway.course.timetable.util;
import com.sunway.course.timetable.model.Venue;

public class VenueDistanceUtils {

    public static double calculateDistance(Venue from, Venue to) {
        if (from.getName().equals(to.getName())) return 0.0;

        String fromFloor = from.getFloor();
        String toFloor = to.getFloor();
        String fromFloorType = from.getFloorType();
        String toFloorType = to.getFloorType();

        int fromLevel = extractFloorLevel(fromFloor);
        int toLevel = extractFloorLevel(toFloor);
        int levelDifference = Math.abs(fromLevel - toLevel);

        boolean isUniversityFrom = fromFloorType.toLowerCase().contains("university");
        boolean isUniversityTo = toFloorType.toLowerCase().contains("university");
        boolean isCollegeFrom = fromFloorType.toLowerCase().contains("college");
        boolean isCollegeTo = toFloorType.toLowerCase().contains("college");

        boolean sameLevel = fromLevel == toLevel;
        boolean sameFloorType = fromFloorType.equalsIgnoreCase(toFloorType);

        if (isUniversityFrom && isUniversityTo && sameLevel && !sameFloorType) return 50.0;
        if (isUniversityFrom && isUniversityTo && !sameLevel) return 50.0 * levelDifference;

        if (isUniversityFrom && isCollegeTo) {
            int adjustedFrom = Math.abs(fromLevel - 3);
            int adjustedTo = Math.abs(toLevel - 3);
            return fromLevel == 3 ? 150.0 * adjustedTo : 150.0 * adjustedFrom * adjustedTo;
        }

        if (isCollegeFrom && isUniversityTo) {
            int adjustedFrom = Math.abs(fromLevel - 3);
            int adjustedTo = Math.abs(toLevel - 3);
            return toLevel == 3 ? 150.0 * adjustedFrom : 150.0 * adjustedFrom * adjustedTo;
        }

        boolean isSW = fromFloorType.toLowerCase().contains("south west") || toFloorType.toLowerCase().contains("south west");
        boolean isSE = fromFloorType.toLowerCase().contains("south east") || toFloorType.toLowerCase().contains("south east");

        if (isCollegeFrom && isCollegeTo && isSW && isSE && !sameLevel) return 50.0 * levelDifference;

        return 20.0;
    }

    public static int extractFloorLevel(String floorLevel) {
        if (floorLevel == null || floorLevel.isEmpty()) return 0;
        if (floorLevel.toLowerCase().contains("ground")) return 0;
        try {
            return Integer.parseInt(floorLevel.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String sanitizeVenueName(String venue) {
        return venue == null ? null : venue.trim().toUpperCase();
    }
}
