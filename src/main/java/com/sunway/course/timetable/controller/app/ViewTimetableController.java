package com.sunway.course.timetable.controller.app;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.SelectionController;
import com.sunway.course.timetable.result.SelectionStateHolder;
import com.sunway.course.timetable.service.NavigationService;


@Component
public class ViewTimetableController extends SelectionController{

    public ViewTimetableController(NavigationService navService, LoginSceneController loginController,
                                SelectionStateHolder stateHolder) {
        super(navService, loginController, stateHolder);
    }
    
    @Override
    protected void initialize() {
        super.initialize(); // Call BaseController's initialize()
        subheading.setText("View Timetable");
    }
}
