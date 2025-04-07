package com.sunway.course.timetable.model.venuedistance;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "venue_distance")
public class VenueDistance {

    @EmbeddedId
    private VenueDistanceId venueDistanceId;

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
