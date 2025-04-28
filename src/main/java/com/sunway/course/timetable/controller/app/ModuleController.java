package com.sunway.course.timetable.controller.app;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.SelectionController;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.view.MainApp;

@Component
public class ModuleController extends SelectionController {
    

    public ModuleController(NavigationService navService, LoginSceneController loginController) {
        super(navService, loginController);
    }

    @Override
    protected void initialize() {
        super.initialize(); // Call BaseController's initialize()
        subheading.setText("View Module");
    }

}
