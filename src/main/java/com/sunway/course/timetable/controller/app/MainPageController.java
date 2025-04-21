package com.sunway.course.timetable.controller.app;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.base.ContentController;
import com.sunway.course.timetable.view.MainApp;


@Component
public class MainPageController extends ContentController{

    public MainPageController(MainApp mainApp) {
        super(mainApp);
    }

    @Override
    protected void initialize(){
        super.initialize(); // Call BaseController's initialize()
        subheading.setText("Dashboard");
    }

}
