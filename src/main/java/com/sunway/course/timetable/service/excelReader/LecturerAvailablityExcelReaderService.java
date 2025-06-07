package com.sunway.course.timetable.service.excelReader;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.helper.ModuleExcelHelper;
import com.sunway.course.timetable.model.WeekDayConstraint;
import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.service.LecturerServiceImpl;
import com.sunway.course.timetable.service.WeekDayConstraintServiceImpl;
import com.sunway.course.timetable.util.ExcelUtil;


@Service
public class LecturerAvailablityExcelReaderService {

    private static Logger logger = LoggerFactory.getLogger(LecturerAvailablityExcelReaderService.class);

    private WeekDayConstraintServiceImpl weekDayConstraintService;
    private LecturerServiceImpl lecturerService;

    public LecturerAvailablityExcelReaderService(WeekDayConstraintServiceImpl weekDayConstraintService, 
                                                  LecturerServiceImpl lecturerService) {
        this.weekDayConstraintService = weekDayConstraintService;
        this.lecturerService = lecturerService;
    }

    
    public List<WeekDayConstraint> readLecturerAvailabilityExcelFile(String filePath) throws FileNotFoundException {
        List<WeekDayConstraint> lecturerUnavalible = new ArrayList<>();

        try (InputStream inputStream = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(inputStream)){
            
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> headerMap = ExcelUtil.getHeaderMap(sheet.getRow(0));

            for(int i = 1; i <= sheet.getLastRowNum(); i++){
                Row row = sheet.getRow(i);
                if(row == null) continue; // Skip empty rows

                String lecturerId = ExcelUtil.getCellValue(row, headerMap.get("LecturerID"));

                boolean monday = ModuleExcelHelper.parseBoolean(ExcelUtil.getCellValue(row, headerMap.get("Monday")));
                boolean tuesday = ModuleExcelHelper.parseBoolean(ExcelUtil.getCellValue(row, headerMap.get("Tuesday")));
                boolean wednesday = ModuleExcelHelper.parseBoolean(ExcelUtil.getCellValue(row, headerMap.get("Wednesday")));
                boolean thursday = ModuleExcelHelper.parseBoolean(ExcelUtil.getCellValue(row, headerMap.get("Thursday")));
                boolean friday = ModuleExcelHelper.parseBoolean(ExcelUtil.getCellValue(row, headerMap.get("Friday")));

                lecturerUnavalible.add(saveLecturerAvailability(lecturerId, monday, tuesday, wednesday, thursday, friday));

            }
            logger.info("Reading lecturer availability from file: {}", filePath);
        } catch (Exception e) {
            logger.error("Error reading lecturer availability Excel file: {}", e.getMessage());
        }

        return lecturerUnavalible;
    }


    private WeekDayConstraint saveLecturerAvailability(String lecturerId, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday) {
        logger.info("Lecturer ID: {}, Monday: {}, Tuesday: {}, Wednesday: {}, Thursday: {}, Friday: {}", 
                    lecturerId, monday, tuesday, wednesday, thursday, friday);

        Optional<Lecturer> optionalLecturer = lecturerService.getLecturerById(Long.parseLong(lecturerId));
        Lecturer lecturer = optionalLecturer.get();

        // Check if constraint already exists
        Optional<WeekDayConstraint> existing = weekDayConstraintService.getWeekDayConstraintByLecturerId(lecturer.getId());
        WeekDayConstraint constraint;

        if (existing.isPresent()) {
            // Update existing record
            constraint = existing.get();
        } else {
            // Create new record
            constraint = new WeekDayConstraint();
            constraint.setLecturer(lecturer);
        }

        constraint.setMonday(monday);
        constraint.setTuesday(tuesday);
        constraint.setWednesday(wednesday);
        constraint.setThursday(thursday);
        constraint.setFriday(friday);

        weekDayConstraintService.addWeekDayConstraint(constraint);

        return constraint;
    }
}
