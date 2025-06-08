package com.sunway.course.timetable.result;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;

/**
 * Represents the result of attempting to assign a venue to a session.
 */
public class ActorAssignmentResult {
    private final boolean success;
    private final String message;
    private final Session session;
    private final Venue assignedVenue;

    public ActorAssignmentResult(boolean success, String message, Session session, Venue assignedVenue) {
        this.success = success;
        this.message = message;
        this.session = session;
        this.assignedVenue = assignedVenue;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Session getSession() {
        return session;
    }

    public Venue getAssignedVenue() {
        return assignedVenue;
    }
}
