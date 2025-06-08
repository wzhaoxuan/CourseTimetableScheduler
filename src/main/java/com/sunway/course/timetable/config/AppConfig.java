package com.sunway.course.timetable.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.sunway.course.timetable.view.MainApp;

import javafx.application.HostServices;

@Configuration
public class AppConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HostServices hostServices() {
        return MainApp.hostServices;  // pull it from static reference
    }

}
