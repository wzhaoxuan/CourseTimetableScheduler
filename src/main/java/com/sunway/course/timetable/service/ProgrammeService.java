package com.sunway.course.timetable.service;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.programme.Programme;
import com.sunway.course.timetable.model.programme.ProgrammeId;
import com.sunway.course.timetable.repository.ProgrammeRepository;

@Service
public class ProgrammeService {

    private final ProgrammeRepository programmeRepository;

    public ProgrammeService(ProgrammeRepository programmeRepository) {
        this.programmeRepository = programmeRepository;
    }

    public Programme getProgrammeById(ProgrammeId id) {
        return programmeRepository.findById(id).orElse(null);
    }

    public Programme saveProgramme(Programme programme) {
        return programmeRepository.save(programme);
    }

    public void deleteProgramme(ProgrammeId id) {
        programmeRepository.deleteById(id);
    }

     public Optional<Programme> getProgrammeByName(String name) {
        return programmeRepository.findByName(name);
    }

}
