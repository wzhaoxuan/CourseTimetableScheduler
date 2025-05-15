package com.sunway.course.timetable.util;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sunway.course.timetable.model.SubjectPlanInfo;
import com.sunway.course.timetable.service.ExcelReaderService;
import com.sunway.course.timetable.service.generator.VenueDistanceGenerator;

@Configuration
public class RunnerUtil {
    Logger logger = LoggerFactory.getLogger(RunnerUtil.class);


    @Bean
    public CommandLineRunner generateVenueDistances(VenueDistanceGenerator venueDistanceGenerator) {
        return args -> {
            logger.info(">>> TESTING LOGGER OUTPUT <<<");
            // Generate venue distances
            venueDistanceGenerator.generateVenueDistances();
            System.out.println("Venue distances saved into database.");
        };
    }

    @Bean
    public CommandLineRunner readExcelFile(ExcelReaderService excelReaderService) {
        return args -> {
            // Read Excel file and process data
            String filePath = "src/main/resources/file/SubjectPlan.xlsx";
            try {
                List<SubjectPlanInfo> subjectPlanInfos = excelReaderService.readExcelFile(filePath);
                for(int i = 0; i < 3; i++){
                    SubjectPlanInfo subjectPlanInfo = subjectPlanInfos.get(i);
                    System.out.println("Subject Plan Info " + (i + 1) + ": " + subjectPlanInfo.getSubjectCode() + ", " + subjectPlanInfo.getSubjectName());
                }

            } catch (Exception e) {
                System.err.println("Error reading Excel file: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
