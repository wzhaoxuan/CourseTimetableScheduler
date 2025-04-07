package com.sunway.course.timetable.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.exception.IdNotFoundException;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.repository.VenueRepository;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    @Autowired
    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }
    public Optional<Venue> getVenueById(Long id) {
        return venueRepository.findById(id);
    }
    public Venue addVenue(Venue venue) {
        return venueRepository.save(venue);
    }
    public Venue updateVenue(Long id, Venue venue) {
        if (venueRepository.existsById(id)) {
            venue.setId(id);
            return venueRepository.save(venue);
        } else {
            throw new IdNotFoundException("Venue not found with id " + id);
        }
    }

}
