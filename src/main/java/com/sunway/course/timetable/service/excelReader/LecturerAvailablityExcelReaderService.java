package com.sunway.course.timetable.service.excelReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.helper.ModuleExcelHelper;
import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.model.WeekDayConstraint;
import com.sunway.course.timetable.service.LecturerServiceImpl;
import com.sunway.course.timetable.service.WeekDayConstraintServiceImpl;
import com.sunway.course.timetable.util.ExcelUtil;
import com.sunway.course.timetable.util.LecturerDayAvailabilityUtil;


@Service
public class LecturerAvailablityExcelReaderService {

    private static Logger logger = LoggerFactory.getLogger(LecturerAvailablityExcelReaderService.class);

    private WeekDayConstraintServiceImpl weekDayConstraintService;
    private LecturerServiceImpl lecturerService;
    private final LecturerDayAvailabilityUtil lecturerDayAvailabilityUtil;

    public LecturerAvailablityExcelReaderService(WeekDayConstraintServiceImpl weekDayConstraintService, 
                                                  LecturerServiceImpl lecturerService,
                                                  LecturerDayAvailabilityUtil lecturerDayAvailabilityUtil) {
        this.weekDayConstraintService = weekDayConstraintService;
        this.lecturerService = lecturerService;
        this.lecturerDayAvailabilityUtil = lecturerDayAvailabilityUtil;
    }

    
    public List<WeekDayConstraint> readLecturerAvailabilityExcelFile(String filePath) throws FileNotFoundException, IOException, IllegalStateException {
        List<WeekDayConstraint> lecturerUnavalible = new ArrayList<>();
        List<String> violations  = new ArrayList<>();

        try (InputStream inputStream = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(inputStream)){
            
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> headerMap = ExcelUtil.getHeaderMap(sheet.getRow(0));

            for(int i = 1; i <= sheet.getLastRowNum(); i++){
                Row row = sheet.getRow(i);
                if(row == null) continue; // Skip empty rows

                String lecturerIdStr = ExcelUtil.getCellValue(row, headerMap.get("LecturerID"));
                if (lecturerIdStr == null || lecturerIdStr.isBlank()) {
                    // skip rows without an ID
                    continue;
                }

                long lecturerId;
                try {
                    lecturerId = Long.parseLong(lecturerIdStr.trim());
                } catch (NumberFormatException e) {
                    // skip rows with malformed ID
                    continue;
                }


                boolean monday = ModuleExcelHelper.parseBoolean(ExcelUtil.getCellValue(row, headerMap.get("Monday")));
                boolean tuesday = ModuleExcelHelper.parseBoolean(ExcelUtil.getCellValue(row, headerMap.get("Tuesday")));
                boolean wednesday = ModuleExcelHelper.parseBoolean(ExcelUtil.getCellValue(row, headerMap.get("Wednesday")));
                boolean thursday = ModuleExcelHelper.parseBoolean(ExcelUtil.getCellValue(row, headerMap.get("Thursday")));
                boolean friday = ModuleExcelHelper.parseBoolean(ExcelUtil.getCellValue(row, headerMap.get("Friday")));

                
                WeekDayConstraint constraint = saveLecturerAvailability(
                    lecturerId, monday, tuesday, wednesday, thursday, friday
                );
                lecturerUnavalible.add(constraint);

                try {
                    lecturerDayAvailabilityUtil.validateLecturerWeekdays(
                        lecturerId, constraint.getLecturer().getName()
                    );
                } catch (IllegalStateException ex) {
                    violations.add(ex.getMessage());
                }
                
            }
            logger.info("Successfully read lecturer availability from file: {}", filePath);
        } catch (IOException ioe) {
            logger.error("I/O error reading lecturer availability Excel file: {}", ioe.getMessage(), ioe);
            throw ioe;
        }

        if (!violations.isEmpty()) {
            logger.error("Found {} availability violations", violations.size());
            throw new IllegalStateException(String.join("\n", violations));
        }

        return lecturerUnavalible;
    }


    private WeekDayConstraint saveLecturerAvailability(Long lecturerId, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday) throws IOException{
        logger.info("Lecturer ID: {}, Monday: {}, Tuesday: {}, Wednesday: {}, Thursday: {}, Friday: {}", 
                    lecturerId, monday, tuesday, wednesday, thursday, friday);

        Optional<Lecturer> optionalLecturer = lecturerService.getLecturerById(lecturerId);
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
