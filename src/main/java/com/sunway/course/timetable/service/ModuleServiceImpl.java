package com.sunway.course.timetable.service;
import com.sunway.course.timetable.interfaces.services.ModuleService;

import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.repository.ModuleRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;

    @Autowired
    public ModuleServiceImpl(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    @Override
    public List<Module> getAllModules() {
        return moduleRepository.findAll();
    }

    @Override
    public Optional<Module> getModuleById(Long id) {
        return moduleRepository.findById(id);
    }
    
    @Override
    public Optional<Module> getModuleByName(String name) {
        return moduleRepository.findByName(name);
    }

    @Override
    public Optional<Module> getModuleCreditHour(int creditHour) {
        if (creditHour <= 0) {
            throw new IllegalArgumentException("Credit hour must be greater than zero");
        }
        return moduleRepository.findByCreditHour(creditHour);
    }

}
