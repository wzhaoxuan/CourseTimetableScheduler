package com.sunway.course.timetable.interfaces.services;
import java.util.List;
import java.util.Optional;

import com.sunway.course.timetable.model.Module;

public interface ModuleService {
    List<Module> getAllModules();
    Optional<Module> getModuleById(String id);
    Optional<Module> getModuleByName(String name);
    Optional<List<Module>> getModuleCreditHour(int creditHour);
    
}
