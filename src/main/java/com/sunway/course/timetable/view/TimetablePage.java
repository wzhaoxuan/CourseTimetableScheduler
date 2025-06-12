package com.sunway.course.timetable.view;

public enum TimetablePage {
    LOGIN("/desktop/course/timetable/LoginScene.fxml"),
    SIGNUP("/desktop/course/timetable/SignUpScene.fxml"),
    MAIN("/desktop/course/timetable/MainPageScene.fxml"),
    GENERATE("/desktop/course/timetable/GenerateTimetableScene.fxml"),
    VIEW("/desktop/course/timetable/ViewTimetableScene.fxml"),
    PROGRAMME("/desktop/course/timetable/ProgrammeScene.fxml"),
    MODULE("/desktop/course/timetable/ModuleScene.fxml"),
    LECTURER("/desktop/course/timetable/LecturerScene.fxml"),
    TIMETABLE("/desktop/course/timetable/TimetableScene.fxml");

    private final String fxmlPath;

    TimetablePage(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}

