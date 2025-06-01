package com.sunway.course.timetable.config;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sunway.course.timetable.akka.actor.VenueActor;
import com.sunway.course.timetable.akka.actor.VenueActor.VenueCommand;
import com.sunway.course.timetable.akka.actor.VenueCoordinatorActor;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.service.venue.VenueSorterService;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import akka.actor.typed.javadsl.Behaviors;

@Configuration
public class AkkaConfig {

    @Bean
    public ActorSystem<Void> actorSystem() {
        return ActorSystem.create(Behaviors.empty(), "FullActorSystem");
    }

    @Bean
    public List<Venue> venueList(VenueSorterService venueSorterService) {
        return venueSorterService.sortByAscendingCapacity(); // or whatever logic
    }

    @Bean
    public List<ActorRef<VenueCommand>> venueActors(
            ActorSystem<Void> actorSystem,
            List<Venue> venueList,
            VenueAvailabilityMatrix venueAvailabilityMatrix,
            LecturerAvailabilityMatrix lecturerAvailabilityMatrix
    ) {
        return venueList.stream()
                .map(venue -> actorSystem.systemActorOf(
                        VenueActor.create(venue, venueAvailabilityMatrix, lecturerAvailabilityMatrix),
                        "venueActor-" + venue.getId(),
                        Props.empty()))
                .collect(Collectors.toList());
    }


    @Bean
    public ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> venueCoordinatorActor(
            ActorSystem<Void> actorSystem,
            List<Venue> venueList,
            List<ActorRef<VenueCommand>> venueActors,
            LecturerAvailabilityMatrix lecturerMatrix,
            VenueAvailabilityMatrix venueAvailabilityMatrix
    ) {
        return actorSystem.systemActorOf(
                VenueCoordinatorActor.create(
                        venueList,
                        venueActors,
                        lecturerMatrix,
                        venueAvailabilityMatrix
                ),
                "venueCoordinator",
                Props.empty()
        );
    }
}

