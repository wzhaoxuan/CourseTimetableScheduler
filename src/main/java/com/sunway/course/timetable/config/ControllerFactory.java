package com.sunway.course.timetable.config;

import com.sunway.course.timetable.controller.MainAppAware;
import com.sunway.course.timetable.view.MainApp;

import javafx.util.Callback;

public class ControllerFactory implements Callback<Class<?>, Object> {

    private final MainApp mainApp;

    public ControllerFactory(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @Override
    public Object call(Class<?> controllerClass) {
        try {
            // Create a new instance
            Object controller = controllerClass.getDeclaredConstructor().newInstance();

            // Inject mainApp instance if controller implements MainAppAware
            if (controller instanceof MainAppAware) {
                ((MainAppAware) controller).setMainApp(mainApp);
            }

            return controller;
        } catch (Exception e) {
            throw new RuntimeException("Could not create controller" + controllerClass, e);
        }
    }

}
