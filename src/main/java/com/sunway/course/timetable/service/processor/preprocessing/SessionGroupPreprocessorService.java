package com.sunway.course.timetable.service.processor.preprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.SubjectPlanInfo;
import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;
import com.sunway.course.timetable.model.assignment.SessionGroupMetaData;

@Service
public class SessionGroupPreprocessorService {
    private final int MAX_GROUP_SIZE = 35;

    /**
     * Prepare session group metadata from raw sessions.
     * Groups sessions by semester, module, and type+group and generates metadata.
     * 
     * @param data ModuleAssignmentData containing module info and eligible students
     * @param studentSemesterMap Mapping of student ID to semester
     * @return List of SessionGroupMetaData, one per group for scheduling
     */
    public List<SessionGroupMetaData> prepareSessionGroupMetadata(ModuleAssignmentData data, Map<Long, Integer> studentSemesterMap) {
        Module module = data.getModule();
        SubjectPlanInfo plan = data.getSubjectPlanInfo();
        Set<Student> allStudents = data.getEligibleStudents();

        Map<Integer, List<Student>> studentsBySemester = new HashMap<>();
        for (Student student : allStudents) {
            Integer semester = studentSemesterMap.get(student.getId());
            if (semester != null) {
                studentsBySemester.computeIfAbsent(semester, k -> new ArrayList<>()).add(student);
            }
        }

        List<SessionGroupMetaData> metaDataList = new ArrayList<>();

        for (var entry : studentsBySemester.entrySet()) {
            int semester = entry.getKey();
            List<Student> students = entry.getValue();
            int totalStudents = students.size();

            if (plan.hasLecture()) {
                String group = plan.getSubjectCode() + "-Lecture-G1";
                metaDataList.add(new SessionGroupMetaData(
                    semester,
                    module.getId(),
                    "Lecture",
                    group,
                    plan.getMainLecturer(),
                    totalStudents,
                    0,
                    1
                ));
            }

            List<SessionTypeInfo> sessionTypes = List.of(
                new SessionTypeInfo("Tutorial", plan.hasTutorial(), plan.getTutorialTutor()),
                new SessionTypeInfo("Practical", plan.hasPractical(), plan.getPracticalTutor()),
                new SessionTypeInfo("Workshop", plan.hasWorkshop(), plan.getWorkshopTutor())
            );

            int groupCount = (int) Math.ceil((double) totalStudents / MAX_GROUP_SIZE);

            for (SessionTypeInfo typeInfo : sessionTypes) {
                if (!typeInfo.hasType) continue;

                List<String> tutors = typeInfo.tutor();

                for (int i = 0; i < groupCount; i++) {
                    String group = plan.getSubjectCode() + "-" + typeInfo.type() + "-G" + (i + 1);
                    String tutor = tutors.isEmpty() ? null : tutors.get(i % tutors.size());

                    SessionGroupMetaData metaData = new SessionGroupMetaData(
                        semester,
                        module.getId(),
                        typeInfo.type(),
                        group,
                        tutor,
                        MAX_GROUP_SIZE,
                        i,
                        groupCount
                    );
                    metaDataList.add(metaData);
                }
            }
        }

        return metaDataList;
    }

    private static record SessionTypeInfo(String type, boolean hasType, List<String> tutor) {}
}

