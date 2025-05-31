package com.sunway.course.timetable.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;

@Configuration
public class AkkaConfig {

    @Bean
    public ActorSystem<Void> actorSystem() {
        return ActorSystem.create(Behaviors.empty(), "VenueActorTest");
    }
}
