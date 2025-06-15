package com.sunway.course.timetable.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Component;

@Component
public class ProgrammeHistoryStorageService {

    private static final String PROGRAMME_HISTORY_DIR = "system_storage/history/programme/";

    public ProgrammeHistoryStorageService() {
        File dir = new File(PROGRAMME_HISTORY_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public void saveProgrammeFile(File sourceFile) {
        File destFile = new File(PROGRAMME_HISTORY_DIR, sourceFile.getName());
        try {
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File[] getProgrammeFiles() {
        File dir = new File(PROGRAMME_HISTORY_DIR);
        return dir.listFiles((d, name) -> name.endsWith(".xlsx"));
    }

    public File[] getProgrammeFilesForCode(String programmeCode) {
        File dir = new File(PROGRAMME_HISTORY_DIR);
        return dir.listFiles((d, name) -> name.startsWith(programmeCode + "-") && name.endsWith(".xlsx"));
    }
}
