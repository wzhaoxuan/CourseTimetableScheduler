package com.sunway.course.timetable.service.excelReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.helper.ModuleExcelHelper;
import com.sunway.course.timetable.model.assignment.ModuleSem;
import com.sunway.course.timetable.util.ExcelUtil;



@Service
public class ModuleSemExcelReaderService {

    private static Logger logger = LoggerFactory.getLogger(ProgrammeExcelReaderService.class);

    public Map<Integer, List<ModuleSem>> readModuleSemExcelFile(String filePath) throws FileNotFoundException {
        Map<Integer, List<ModuleSem>> moduleSemMap = new HashMap<>();
        try (InputStream inputStream = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> headerMap = ExcelUtil.getHeaderMap(sheet.getRow(0));

            for(int i = 1; i <= sheet.getLastRowNum(); i++){
                Row row = sheet.getRow(i);
                if(row == null) continue; // Skip empty rows

                int semester = ModuleExcelHelper.parseInt(ExcelUtil.getCellValue(row, headerMap.get("Semester")));
                String moduleCode = ExcelUtil.getCellValue(row, headerMap.get("ModuleID"));

                ModuleSem moduleSem = new ModuleSem(moduleCode);

                // Add the ModuleSem object to the map under the corresponding semester
                moduleSemMap.computeIfAbsent(semester, k -> new ArrayList<>()).add(moduleSem);

            }
        } catch (IOException e) {
            logger.error("Error reading Module Semester Excel file: {}", e.getMessage());
        }
        // logger.info("Module Semester Map: {}", moduleSemMap);
        return moduleSemMap;
    }

}
