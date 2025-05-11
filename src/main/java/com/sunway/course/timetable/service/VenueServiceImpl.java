package com.sunway.course.timetable.service;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.event.VenueAddedEvent;
import com.sunway.course.timetable.exception.CreationException;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.repository.VenueRepository;
import com.sunway.course.timetable.interfaces.services.VenueService;

@Service
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VenueServiceImpl(VenueRepository venueRepository, ApplicationEventPublisher eventPublisher) {
        this.venueRepository = venueRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    @Override
    public Optional<Venue> getVenueByName(String name) {
        return venueRepository.findByName(name);
    }

    @Override
    public Optional<Venue> getVenueById(Long id) {
        return venueRepository.findById(id);
    }

    @Override
    public Venue addVenue(Venue venue) {
        try{
            return venueRepository.save(venue);
        } catch (Exception e) {
            throw new CreationException("Failed to add venue", e);
        }
    }

    @Override
    public void publishVenueAddedEvent(String name) {
        if(getVenueByName(name).isPresent()) {
            Venue venueName = getVenueByName(name).get();
            eventPublisher.publishEvent(new VenueAddedEvent(venueName));
        } else {
            System.out.println("Venue not found with name: " + name);
        }
    }   
}
