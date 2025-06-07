package com.sunway.course.timetable.model.venueAssignment;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "venue_assignment")
public class VenueAssignment {

    @EmbeddedId
    private VenueAssignmentId venueAssignmentId;

    @ManyToOne
    @MapsId("venueId")
    @JoinColumn(name = "venue_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Venue venue;

    @ManyToOne
    @MapsId("sessionId")
    @JoinColumn(name = "session_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Session session;

    public VenueAssignment() {
        // Default constructor
    }

    public VenueAssignment(VenueAssignmentId venueAssignmentId) {
        this.venueAssignmentId = venueAssignmentId;
    }

    public VenueAssignmentId getVenueAssignmentId() {
        return venueAssignmentId;
    }

    public void setVenueAssignmentId(VenueAssignmentId venueAssignmentId) {
        this.venueAssignmentId = venueAssignmentId;
    }

    public Venue getVenue(){
        return venue;
    }

    public void setVenue(Venue venue){
        this.venue = venue;
    }

    public Session getSession(){
        return session;
    }
    
    public void setSession(Session session){
        this.session = session;
    }
}
