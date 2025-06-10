package com.sunway.course.timetable.result;
import com.sunway.course.timetable.model.Session;
import java.io.File;
import java.util.List;
import java.util.Map;
/**
 * Represents the final result of the assignment process, including the session map,
 * exported timetable files, and the fitness score.
 */
public class FinalAssignmentResult {
    private final Map<Integer, Map<String, List<Session>>> sessionMap;
    private final List<File> exportedTimetableFiles;
    private final List<File> lecturerTimetableFiles;
    private final double fitnessScore;

    public FinalAssignmentResult(
        Map<Integer, Map<String, List<Session>>> sessionMap,
        List<File> exportedTimetableFiles,
        List<File> lecturerTimetableFiles,
        double fitnessScore
    ) {
        this.sessionMap = sessionMap;
        this.exportedTimetableFiles = exportedTimetableFiles;
        this.lecturerTimetableFiles = lecturerTimetableFiles;
        this.fitnessScore = fitnessScore;
    }

    public Map<Integer, Map<String, List<Session>>> getSessionMap() {
        return sessionMap;
    }

    public List<File> getExportedTimetableFiles() {
        return exportedTimetableFiles;
    }

    public List<File> getLecturerTimetableFiles(){
        return lecturerTimetableFiles;
    }

    public double getFitnessScore() {
        return fitnessScore;
    }
}
