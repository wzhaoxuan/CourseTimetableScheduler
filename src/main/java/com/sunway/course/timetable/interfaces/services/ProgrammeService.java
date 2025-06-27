package com.sunway.course.timetable.interfaces.services;

import java.util.Optional;

import com.sunway.course.timetable.model.programme.Programme;
import com.sunway.course.timetable.model.programme.ProgrammeId;

public interface ProgrammeService {
    Optional<Programme> getProgrammeById(ProgrammeId id);
    Programme saveProgramme(Programme programme);
    void deleteProgramme(ProgrammeId id);
    Optional<Programme> getProgrammeByName(String name);
}
