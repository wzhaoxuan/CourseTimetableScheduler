package com.sunway.course.timetable.service;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.view.MainApp;

@Service
public class NavigationService {

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    private void checkMainApp() {
        if (mainApp == null) {
            throw new IllegalStateException("MainApp not injected into NavigationService");
        }
    }

    public MainApp getMainApp() {
        return mainApp;
    }

    public void loadMainPage() throws Exception {
        checkMainApp();
        mainApp.loadMainPage();
    }

    public void loadLoginPage() throws Exception {
        checkMainApp();
        mainApp.loadLoginPage();
    }

    public void loadSignUpPage() throws Exception {
        checkMainApp();
        mainApp.loadSignUpPage();
    }

    public void loadGenerateTimetablePage() throws Exception {
        checkMainApp();
        mainApp.loadGenerateTimetablePage();
    }

    public void loadViewTimetablePage() throws Exception {
        checkMainApp();
        mainApp.loadViewTimetablePage();
    }

    public void loadProgrammePage() throws Exception {
        checkMainApp();
        mainApp.loadProgrammePage();
    }

    public void loadLecturerPage() throws Exception {
        checkMainApp();
        mainApp.loadLecturerPage();
    }

    public void loadModulePage() throws Exception {
        checkMainApp();
        mainApp.loadModulePage();
    }

    public void loadTimetablePage() throws Exception {
        checkMainApp();
        mainApp.loadTimetablePage();
    }
}
