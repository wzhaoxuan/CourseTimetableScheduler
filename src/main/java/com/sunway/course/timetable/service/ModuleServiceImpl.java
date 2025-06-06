package com.sunway.course.timetable.service;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.interfaces.services.ModuleService;
import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.repository.ModuleRepository;

@Service
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;

    public ModuleServiceImpl(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    @Override
    public List<Module> getAllModules() {
        return moduleRepository.findAll();
    }

    @Override
    public Optional<Module> getModuleById(String id) {
        return moduleRepository.findById(id);
    }
    
    @Override
    public Optional<Module> getModuleByName(String name) {
        return moduleRepository.findByName(name);
    }

    @Override
    public Optional<List<Module>> getModuleCreditHour(int creditHour) {
        if (creditHour <= 0) {
            throw new IllegalArgumentException("Credit hour must be greater than zero");
        }
        return moduleRepository.findByCreditHour(creditHour);
    }

}
