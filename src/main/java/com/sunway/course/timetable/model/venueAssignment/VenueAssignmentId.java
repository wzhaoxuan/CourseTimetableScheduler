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

    @Column(name = "version_tag")
    private String versionTag;

    //Defautl constructor
    public VenueAssignmentId() {
    }

    public VenueAssignmentId(Long venueId, Long sessionId, String versionTag) {
        this.venueId = venueId;
        this.sessionId = sessionId;
        this.versionTag = versionTag;
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

    public String getversionTag() {
        return versionTag;
    }

    public void setversionTag(String versionTag) {
        this.versionTag = versionTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VenueAssignmentId)) return false;
        VenueAssignmentId that = (VenueAssignmentId) o;
        return Objects.equals(sessionId, that.sessionId)
            && Objects.equals(venueId,   that.venueId)
            && Objects.equals(versionTag, that.versionTag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(venueId, sessionId, versionTag);
    }
}
