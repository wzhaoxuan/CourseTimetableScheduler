package com.sunway.course.timetable.service;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.SubjectPlanInfo;
import com.sunway.course.timetable.util.ExcelUtil;

@Service
public class ExcelReaderService {

    public List<SubjectPlanInfo> readExcelFile(String filePath) throws IOException, InvalidFormatException {
        List<SubjectPlanInfo> subjectPlanInfos = new ArrayList<>();

        try(InputStream inputStream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean isHeaderRow = true;

            for(Row row : sheet){
                if(isHeaderRow){
                    isHeaderRow = false;
                    continue; // Skip header row
                }

                String subjectCode = ExcelUtil.getCellValue(row.getCell(0));
                String subjectName = ExcelUtil.getCellValue(row.getCell(1));
                boolean lecture = ExcelUtil.parseBoolean(ExcelUtil.getCellValue(row.getCell(2)));
                boolean practical = ExcelUtil.parseBoolean(ExcelUtil.getCellValue(row.getCell(3)));
                boolean tutorial = ExcelUtil.parseBoolean(ExcelUtil.getCellValue(row.getCell(4)));
                boolean workshop = ExcelUtil.parseBoolean(ExcelUtil.getCellValue(row.getCell(5)));
                int totalStudents = ExcelUtil.parseInt(ExcelUtil.getCellValue(row.getCell(6)));
                String mainlecturer = ExcelUtil.getCellValue(row.getCell(7));
                String practicalTutor = ExcelUtil.getCellValue(row.getCell(8));
                String tutorialTutor = ExcelUtil.getCellValue(row.getCell(9));
                String workshopTutor = ExcelUtil.getCellValue(row.getCell(10));

                SubjectPlanInfo subjectPlanInfo = new SubjectPlanInfo(subjectCode, subjectName, lecture, practical, tutorial, workshop,
                        totalStudents, mainlecturer, practicalTutor, tutorialTutor, workshopTutor);

                subjectPlanInfos.add(subjectPlanInfo);

            }
            
        }

        return subjectPlanInfos;
    }

}