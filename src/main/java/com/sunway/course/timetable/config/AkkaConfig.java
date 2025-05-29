package com.sunway.course.timetable.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sunway.course.timetable.actor.VenueCoordinatorActor;
import com.sunway.course.timetable.repository.VenueRepository;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import com.sunway.course.timetable.model.Venue;
import java.util.List;


@Configuration
public class AkkaConfig {

    private final VenueRepository venueRepository;

    public AkkaConfig(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Bean(name = "venueCoordinatorActorSystem")
    public ActorSystem<VenueCoordinatorActor.Command> venueCoordinatorActorSystem() {
        List<Venue> venues = venueRepository.findAll();
        return ActorSystem.create(VenueCoordinatorActor.create(venues), "VenueCoordinatorSystem");
    }

    @Bean(name = "generalActorSystem")
    public ActorSystem<?> generalActorSystem() {
        // You can use a guardian behavior like Behaviors.empty(), or define your root actor
        return ActorSystem.create(Behaviors.empty(), "GeneralActorSystem");
    }

}
