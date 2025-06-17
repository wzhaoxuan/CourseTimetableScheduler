package com.sunway.course.timetable.engine;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.assignment.SessionGroupMetaData;


public class UnassignedStudentResolver {

    private static final Logger log = LoggerFactory.getLogger(UnassignedStudentResolver.class);

    public List<SessionGroupMetaData> resolve(
        List<SessionGroupMetaData> failedMeta,
        Map<SessionGroupMetaData, List<Student>> studentAssignments
    ) {
        List<SessionGroupMetaData> extraGroups = new ArrayList<>();

        // Group by Module + Type (Lecture/Practical/Tutorial)
        Map<String, List<SessionGroupMetaData>> groupedMeta = failedMeta.stream()
            .collect(Collectors.groupingBy(meta -> meta.getModuleId() + "-" + meta.getType()));

        for (Map.Entry<String, List<SessionGroupMetaData>> entry : groupedMeta.entrySet()) {
            List<SessionGroupMetaData> metaList = entry.getValue();

            // Build set of assigned students
            Set<Long> assignedStudents = metaList.stream()
                .flatMap(meta -> studentAssignments.getOrDefault(meta, List.of()).stream())
                .map(Student::getId)
                .collect(Collectors.toSet());

            // Collect all eligible students for this module-type
            Set<Student> eligibleStudents = metaList.stream()
                .flatMap(meta -> meta.getEligibleStudents().stream())
                .collect(Collectors.toSet());

            // Detect unassigned students
            List<Student> unassigned = eligibleStudents.stream()
                .filter(s -> !assignedStudents.contains(s.getId()))
                .toList();

            if (!unassigned.isEmpty()) {
                log.warn("Found {} unassigned students for {}", unassigned.size(), entry.getKey());

                // Create new group index (increment highest existing groupIndex)
                int nextGroupIndex = metaList.stream().mapToInt(SessionGroupMetaData::getGroupIndex).max().orElse(0) + 1;
                int groupCount = metaList.get(0).getGroupCount() + 1;

                SessionGroupMetaData base = metaList.get(0); // Use one existing meta as template

                SessionGroupMetaData newGroup = new SessionGroupMetaData();
                newGroup.setModuleId(base.getModuleId());
                newGroup.setType(base.getType());
                newGroup.setTypeGroup(base.getModuleId() + "-" + base.getType() + "-G" + (nextGroupIndex + 1));
                newGroup.setLecturerName(base.getLecturerName());  // You may dynamically assign lecturer here
                newGroup.setGroupIndex(nextGroupIndex);
                newGroup.setGroupCount(groupCount);
                newGroup.setTotalStudents(unassigned.size());
                newGroup.setEligibleStudents(unassigned);
                newGroup.setSemester(base.getSemester());

                extraGroups.add(newGroup);
            }
        }

        return extraGroups;
    }
}

