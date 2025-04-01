package com.sunway.course.timetable.controller.app;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.base.SelectionController;
import com.sunway.course.timetable.view.MainApp;

@Component
public class ViewTimetableController extends SelectionController{

    public ViewTimetableController(MainApp mainApp) {
        super(mainApp);
    }

    @Override
    protected void initialize() {
        super.initialize(); // Call BaseController's initialize()
        subheading.setText("View Timetable");
    }
}
