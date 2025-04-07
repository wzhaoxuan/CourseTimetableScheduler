package com.sunway.course.timetable;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import com.sunway.course.timetable.view.MainApp;

import javafx.application.Application;

@SpringBootApplication(scanBasePackages = "com.sunway.course.timetable") // Spring Boot application
@EntityScan(basePackages = "com.sunway.course.timetable.model") // Scan for JPA entities in the specified package
// This is necessary for JPA to find the entity classes
public class CourseTimetableSchedularApplication {

	public static void main(String[] args) {
		Application.launch(MainApp.class, args); // Launch JavaFX application

	}

}
