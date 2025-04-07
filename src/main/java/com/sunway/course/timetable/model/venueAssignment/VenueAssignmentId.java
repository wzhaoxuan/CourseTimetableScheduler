package com.sunway.course.timetable.model.venueAssignment;
import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class VenueAssignmentId implements Serializable{

    @Column(name = "venue_id")
    private Long venueId;
    
    @Column(name = "session_id")
    private Long sessionId;

    //Defautl constructor
    public VenueAssignmentId() {
    }

    public VenueAssignmentId(Long venueId, Long sessionId) {
        this.venueId = venueId;
        this.sessionId = sessionId;
    }

    public Long getVenueId() {
        return venueId;
    }

    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VenueAssignmentId)) return false;
        VenueAssignmentId that = (VenueAssignmentId) o;
        return venueId == that.venueId && sessionId == that.sessionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(venueId, sessionId);
    }

}
