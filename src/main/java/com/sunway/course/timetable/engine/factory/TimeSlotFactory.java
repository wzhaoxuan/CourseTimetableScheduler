package com.sunway.course.timetable.engine.factory;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.sunway.course.timetable.engine.TimeSlot;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.venuedistance.VenueDistance;

public class TimeSlotFactory {

    /**
     * Generate TimeSlots for all venues ordered by distance from originVenue.
     * @param venues all possible venues
     * @param venueDistances map keyed by venueId from originVenue, value is VenueDistance entity
     * @param originVenue the venue to measure distance from
     * @return List of TimeSlots ordered by day, time, and venue distance
     */
    public static List<TimeSlot> generateTimeSlotsByVenueDistance(
            List<Venue> venues,
            Map<Long, VenueDistance> venueDistances,
            Venue originVenue) {

        List<TimeSlot> slots = new ArrayList<>();

        int durationHours = 2;
        DayOfWeek[] weekdays = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};

        // Sort venues by distance from originVenue (closest first)
        venues.sort(Comparator.comparingDouble(
            v -> {
                VenueDistance vd = venueDistances.get(v.getId());
                return vd != null ? vd.getDistance() : Double.MAX_VALUE;
            }));

        for (DayOfWeek day : weekdays) {
            LocalTime start = LocalTime.of(8, 0);
            LocalTime latestStart = LocalTime.of(18 - durationHours, 0);

            while (!start.isAfter(latestStart)) {
                LocalTime end = start.plusHours(durationHours);
                for (Venue venue : venues) {
                    VenueDistance vd = venueDistances.get(venue.getId());
                    TimeSlot ts = new TimeSlot(day, start, end, venue, vd);
                    slots.add(ts);
                }
                start = start.plusMinutes(30);
            }
        }
        return slots;
    }
}
