package com.sunway.course.timetable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sunway.course.timetable.view.MainApp;

import javafx.application.Application;

@SpringBootApplication
public class CourseTimetableSchedularApplication {

	public static void main(String[] args) {
		Application.launch(MainApp.class, args); // Launch JavaFX application
		SpringApplication.run(CourseTimetableSchedularApplication.class, args);

	}

}
