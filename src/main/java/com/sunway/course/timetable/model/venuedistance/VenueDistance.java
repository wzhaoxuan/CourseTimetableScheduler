package com.sunway.course.timetable.model.venuedistance;
import com.sunway.course.timetable.model.Venue;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "venue_distance")
public class VenueDistance {

    @EmbeddedId
    private VenueDistanceId venueDistanceId;

    @ManyToOne
    @MapsId("venueId") // Maps venueId from the embedded composite key
    @JoinColumn(name = "venue_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Venue venue;

    @Column(nullable=false)
    private Double distance;

    public VenueDistance() {
        // Default constructor
    }

    public VenueDistance(VenueDistanceId venueDistanceId, Double distance) {
        this.venueDistanceId = venueDistanceId;
        this.distance = distance;
    }

    public VenueDistanceId getVenueDistanceId() {
        return venueDistanceId;
    }

    public void setVenueDistanceId(VenueDistanceId venueDistanceId) {
        this.venueDistanceId = venueDistanceId;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}
