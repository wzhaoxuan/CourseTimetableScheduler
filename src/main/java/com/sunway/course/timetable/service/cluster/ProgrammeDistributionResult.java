package com.sunway.course.timetable.service.cluster;
import java.util.Map;

import com.sunway.course.timetable.model.assignment.SessionGroupMetaData;

public record ProgrammeDistributionResult(
    Map<Integer, Map<String, Map<String, Double>>> percentageMap,
    Map<SessionGroupMetaData, String> majorityProgrammeByGroup
    ) {}
