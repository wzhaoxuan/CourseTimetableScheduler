package com.sunway.course.timetable.util;
import com.sunway.course.timetable.engine.DomainPruner.AssignmentOption;
import com.sunway.course.timetable.service.venue.VenueDistanceServiceImpl;
import java.util.Comparator;


public class DomainOrderingUtil {

    public static Comparator<AssignmentOption> standardOrdering() {
        return Comparator
            .comparingInt(AssignmentOption::day)
            .thenComparingInt(AssignmentOption::startSlot)
            .thenComparingInt(opt -> opt.venue().getCapacity());
    }

    public static Comparator<AssignmentOption> backtrackingOrdering(VenueDistanceServiceImpl venueDistanceService, String referenceVenue, int requiredCapacity) {
        return Comparator
            .comparingInt(AssignmentOption::day)
            .thenComparingInt(AssignmentOption::startSlot)
            .thenComparingInt((AssignmentOption opt) -> {
                int surplus = opt.venue().getCapacity() - requiredCapacity;
                return (surplus < 0) ? Integer.MAX_VALUE : surplus;
            })
            .thenComparingDouble(opt -> venueDistanceService.getDistanceScore(referenceVenue, opt.venue().getName()));
    }
}

