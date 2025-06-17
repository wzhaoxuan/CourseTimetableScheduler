package com.sunway.course.timetable.engine;
import com.sunway.course.timetable.model.assignment.SessionGroupMetaData;

public class DomainRejectionReason {
    private final SessionGroupMetaData meta;
    private final int day;
    private final int startSlot;
    private final String venueName;
    private final String reason;

    public DomainRejectionReason(SessionGroupMetaData meta, int day, int startSlot, String venueName, String reason) {
        this.meta = meta;
        this.day = day;
        this.startSlot = startSlot;
        this.venueName = venueName;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return String.format(
            "Rejected - Module: %s Group: %s Day: %d Slot: %d Venue: %s Reason: %s",
            meta.getModuleId(),
            meta.getTypeGroup(),
            day,
            startSlot,
            venueName,
            reason
        );
    }
}


