package com.sunway.course.timetable.controller.app;
import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.base.ContentController;
import com.sunway.course.timetable.service.NavigationService;


@Component
public class MainPageController extends ContentController{

    public MainPageController(NavigationService navService, LoginSceneController loginController) {
        super(navService, loginController);
    }

    @Override
    protected void initialize(){
        super.initialize(); // Call BaseController's initialize()
        subheading.setText("Dashboard");
    }

}
