package com.sunway.course.timetable.model.venuedistance;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class VenueDistanceId {

    @Column(name = "venue_from")
    private String venueFrom;

    @Column(name = "venue_to")
    private String venueTo;

    public VenueDistanceId() {
        // Default constructor
    }

    public VenueDistanceId(String venueFrom, String venueTo) {
        this.venueFrom = venueFrom;
        this.venueTo = venueTo;
    }

    public String getVenueFrom() {
        return venueFrom;
    }

    public void setVenueFrom(String venueFrom) {
        this.venueFrom = venueFrom;
    }

    public String getVenueTo() {
        return venueTo;
    }

    public void setVenueTo(String venueTo) {
        this.venueTo = venueTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VenueDistanceId)) return false;
        VenueDistanceId that = (VenueDistanceId) o;
        return venueFrom.equals(that.venueFrom) && venueTo.equals(that.venueTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash( venueFrom, venueTo);
    }
}
