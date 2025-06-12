package com.sunway.course.timetable.result;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;


@Component
public class MainPageStateHolder {

    private List<File> latestSemesterFiles = new ArrayList<>();

    public synchronized void updateSemesterFiles(List<File> files) {
        latestSemesterFiles.clear();
        latestSemesterFiles.addAll(files);
    }

    public synchronized List<File> getLatestSemesterFiles() {
        return new ArrayList<>(latestSemesterFiles);
    }
}

