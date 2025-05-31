package com.sunway.course.timetable.service.excelReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.helper.ModuleExcelHelper;
import com.sunway.course.timetable.model.SubjectPlanInfo;
import com.sunway.course.timetable.service.generator.ModuleGenerator;
import com.sunway.course.timetable.util.ExcelUtil;

@Service
public class SubjectPlanExcelReaderService {
    private final ModuleGenerator moduleGenerator;

    public SubjectPlanExcelReaderService(ModuleGenerator moduleGenerator) {
        this.moduleGenerator = moduleGenerator;
    }

    public List<SubjectPlanInfo> readExcelFile(String filePath) throws IOException, InvalidFormatException {
        List<SubjectPlanInfo> subjectPlanInfos = new ArrayList<>();

        try(InputStream inputStream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> headerMap = ExcelUtil.getHeaderMap(sheet.getRow(0));

            for(int i = 1; i <= sheet.getLastRowNum(); i++){
                Row row = sheet.getRow(i);
                if(row == null) continue; // Skip empty rows

                String subjectCode = ExcelUtil.getCellValue(row, headerMap.get("Subject Code"));
                String subjectName = ExcelUtil.getCellValue(row, headerMap.get("Subject Name"));
                moduleGenerator.saveModulesFromExcel(subjectCode, subjectName);

                boolean lecture = ModuleExcelHelper.parseBoolean(ExcelUtil.getCellValue(row, headerMap.get("Lecture")));
                boolean practical = ModuleExcelHelper.parseBoolean(ExcelUtil.getCellValue(row, headerMap.get("Practical")));
                boolean tutorial = ModuleExcelHelper.parseBoolean(ExcelUtil.getCellValue(row, headerMap.get("Tutorial")));
                boolean workshop = ModuleExcelHelper.parseBoolean(ExcelUtil.getCellValue(row, headerMap.get("Workshop")));

                int totalStudents = ModuleExcelHelper.parseInt(ExcelUtil.getCellValue(row, headerMap.get("Total Estimated Students")));
                
                String mainlecturer = ExcelUtil.getCellValue(row, headerMap.get("Lecturer"));
                List<String> practicalTutor = splitByComma(ExcelUtil.getCellValue(row, headerMap.get("PracticalTutor")));
                List<String> tutorialTutor = splitByComma(ExcelUtil.getCellValue(row, headerMap.get("TutorialTutor")));
                List<String> workshopTutor = splitByComma(ExcelUtil.getCellValue(row, headerMap.get("WorkshopTutor")));

                SubjectPlanInfo subjectPlanInfo = new SubjectPlanInfo(subjectCode, subjectName, lecture, practical, tutorial, workshop,
                        totalStudents, mainlecturer, practicalTutor, tutorialTutor, workshopTutor);

                subjectPlanInfos.add(subjectPlanInfo);
            }
            
        }

        return subjectPlanInfos;
    }

    private List<String> splitByComma(String input) {
        if (input == null || input.isBlank()) return Collections.emptyList();
        return Arrays.stream(input.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
    }
}