package com.sunway.course.timetable.config;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sunway.course.timetable.controller.app.ProgrammeController;
import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.exporter.HistoricalTimetableExporter;
import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.service.PlanServiceImpl;

import javafx.application.HostServices;


// @Configuration
// public class ControllerConfig {

//     @Bean
//     public ProgrammeController programmeController(
//         NavigationService navigationService,
//         LoginSceneController loginSceneController,
//         HistoricalTimetableExporter historicalExporter,
        
//     ) {
//         return new ProgrammeController(
//             navigationService, loginSceneController,
//             historicalExporter, moduleDataMap, planService,
//             hostServices
//         );
//     }
// }

