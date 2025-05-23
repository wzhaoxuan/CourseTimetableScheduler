package com.sunway.course.timetable;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = CourseTimetableSchedularApplication.class)
@ActiveProfiles("test") // Tells Spring to ignore beans marked with @Profile("!test")
class CourseTimetableSchedularApplicationTests {

	@Test
	void contextLoads() {
	}

}
