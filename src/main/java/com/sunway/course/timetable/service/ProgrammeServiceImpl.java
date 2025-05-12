package com.sunway.course.timetable.service;
import com.sunway.course.timetable.interfaces.services.ProgrammeService;
import java.util.Optional;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.programme.Programme;
import com.sunway.course.timetable.model.programme.ProgrammeId;
import com.sunway.course.timetable.repository.ProgrammeRepository;

@Service
public class ProgrammeServiceImpl implements ProgrammeService {

    private final ProgrammeRepository programmeRepository;

    public ProgrammeServiceImpl(ProgrammeRepository programmeRepository) {
        this.programmeRepository = programmeRepository;
    }

    @Override
    public Optional<Programme> getProgrammeById(ProgrammeId id) {
        return programmeRepository.findById(id);
    }

    @Override
    public Programme saveProgramme(Programme programme) {
        return programmeRepository.save(programme);
    }

    @Override
    public void deleteProgramme(ProgrammeId id) {
        programmeRepository.deleteById(id);
    }

    @Override
     public Optional<Programme> getProgrammeByName(String name) {
        return programmeRepository.findByName(name);
    }

    @Override
    public List<Programme> getProgrammesByYear(int year) {
        return programmeRepository.findByYear(year);
    }

    @Override
    public List<Programme> getProgrammesByIntake(String intake) {
        return programmeRepository.findByIntake(intake);
    }

    @Override
    public List<Programme> getProgrammesBySemester(int semester) {
        return programmeRepository.findBySemester(semester);
    }

}
