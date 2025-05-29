package com.sunway.course.timetable.service.processor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.engine.ConstraintEngine;
import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.SubjectPlanInfo;
import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;
import com.sunway.course.timetable.service.LecturerServiceImpl;
import com.sunway.course.timetable.service.cluster.ProgrammeDistributionClustering;
import com.sunway.course.timetable.service.tracker.CreditHourTracker;
import com.sunway.course.timetable.util.FilterUtil;

import javafx.util.Pair;

/**
 * Service class responsible for processing module assignments by:
 * - Grouping students based on intake months.
 * - Allocating students into sessions (lecture, practical, tutorial, workshop).
 * - Enforcing credit hour limits using CreditHourTracker.
 * - Optionally applying constraints for scheduling (currently commented out).
 */

@Service
public class ModuleAssignmentProcessor {

    private final int MAX_GROUP_SIZE = 35;
    private final CreditHourTracker creditTracker;
    private final LecturerServiceImpl lecturerService;
    private final ConstraintEngine constraintEngine;
    private final ProgrammeDistributionClustering clustering;

    public ModuleAssignmentProcessor(LecturerServiceImpl lecturerService,
                                      ConstraintEngine constraintEngine,
                                      ProgrammeDistributionClustering clustering) {
        this.lecturerService = lecturerService;
        this.constraintEngine = constraintEngine;
        this.clustering = clustering;
        this.creditTracker = new CreditHourTracker();
    }

    /**
     * Main entry point to process and assign sessions based on module data.
     *
     * @param moduleDataList List of ModuleAssignmentData containing module, subject plan, and programmes.
     * @return List of generated sessions (lectures, tutorials, etc.)
     */
    public List<Session> processAssignments(List<ModuleAssignmentData> moduleDataList) {
        List<Session> sessions = new ArrayList<>();

        for(ModuleAssignmentData data : moduleDataList) {
            SubjectPlanInfo plan = data.getSubjectPlanInfo();
            Module module = data.getModule();
            Set<Student> eligibleStudents = data.getEligibleStudents();
            List<Student> filteredStudents = creditTracker.filterEligible(new ArrayList<>(eligibleStudents), module.getCreditHour());

            // Print debug info
            System.out.println("Processing module: " + module.getName() + " (" + module.getId() + ")");
            System.out.println(" - Eligible students: " + eligibleStudents.size());

            int totalStudentsAllowed = plan.getTotalStudents();
            int groupCount = calculateGroupCount(filteredStudents.size());

            List<List<Student>> groups = assignStudentsToGroups(filteredStudents, module, totalStudentsAllowed, groupCount);
            createSessionsForGroups(sessions, groups, plan, groupCount);
        }

        System.out.println("Total sessions created: " + sessions.size());
        // Apply constraints to the sessions
        // System.out.println("Engine started");
        // constraintEngine.scheduleSessions(sessions);
        
        return sessions;

    }

    /**
     * Process module assignments, create sessions, and perform clustering
     * to identify programme distribution per module.
     *
     * @param moduleDataList List of module assignment data
     * @return Pair containing:
     *         - List of created sessions (not saved to DB yet)
     *         - Map of module code -> (programme name -> percentage of students)
     */
    public Pair<List<Session>, Map<String, Map<String, Double>>> clusterProgrammeDistribution(List<ModuleAssignmentData> moduleDataList,
                                                                                                Map<Long, String> studentProgrammeMap,
                                                                                                Map<Long, Integer> studentSemesterMap) {
        List<Session> sessions = new ArrayList<>();

        for (ModuleAssignmentData data : moduleDataList) {
            SubjectPlanInfo plan = data.getSubjectPlanInfo();
            Module module = data.getModule();
            Set<Student> eligibleStudents = data.getEligibleStudents();
            List<Student> filteredStudents = creditTracker.filterEligible(new ArrayList<>(eligibleStudents), module.getCreditHour());

            int totalStudentsAllowed = plan.getTotalStudents();
            int groupCount = calculateGroupCount(filteredStudents.size());

            List<List<Student>> groups = assignStudentsToGroups(filteredStudents, module, totalStudentsAllowed, groupCount);
            createSessionsForGroups(sessions, groups, plan, groupCount);
        }

        // Compute the percentage distribution of students’ programmes by semester
        Map<Integer, Map<String, Map<String, Double>>> distribution =
                clustering.calculateProgrammePercentageDistributionBySemester(sessions, studentProgrammeMap, studentSemesterMap);

        // Combine all semester maps into one map: moduleCode -> (programme -> percentage)
        Map<String, Map<String, Double>> flatDistribution = new HashMap<>();
        for (Map<String, Map<String, Double>> moduleMap : distribution.values()) {
            for (Map.Entry<String, Map<String, Double>> entry : moduleMap.entrySet()) {
                flatDistribution.merge(entry.getKey(), entry.getValue(), (existing, newMap) -> {
                    newMap.forEach((k, v) -> existing.merge(k, v, Double::sum));
                    return existing;
                });
            }
        }
       
        // Optional: print the distribution for debugging
        distribution.forEach((semester, moduleMap) -> {
            System.out.println(">> Semester " + semester);
            moduleMap.forEach((moduleCode, programmeMap) -> {
                System.out.printf("Module %s:\n", moduleCode);
                programmeMap.forEach((programme, percentage) -> {
                    System.out.printf("  - %s: %.2f%%\n", programme, percentage);
                });
            }); 
        });

        // Return both sessions and clustering result
        return new Pair<>(sessions, flatDistribution);
    }

    /**
     * Calculates the number of groups required based on total allowed students and max group size.
     *
     * @param totalStudentsAllowed Max number of students allowed to attend the session
     * @return Number of student groups required
     */
    private int calculateGroupCount(int totalStudentsAllowed) {
        return (int) Math.ceil((double) totalStudentsAllowed / MAX_GROUP_SIZE);
    }

    // Assigns students to groups based on pre-filtered students for semester.
    private List<List<Student>> assignStudentsToGroups(
            List<Student> students,
            Module module,
            int totalStudentsAllowed,
            int groupCount) {

        List<List<Student>> groups = new ArrayList<>();
        for (int i = 0; i < groupCount; i++) {
            groups.add(new ArrayList<>());
        }


        int totalAssigned = 0;
        int currentGroupIndex = 0;

         for (Student student : students) {
            if (totalAssigned >= totalStudentsAllowed) {
                System.out.println(" - Reached total students allowed: " + totalStudentsAllowed);
                break;
            }
            creditTracker.deductCredits(student, module.getCreditHour());

            groups.get(currentGroupIndex).add(student);
            totalAssigned++;
            currentGroupIndex = (currentGroupIndex + 1) % groupCount;
        }
        return groups;
    }

    /**
     * Assigns eligible students to groups while respecting the credit hour limit.
     *
     * @param students             Set of eligible students for the module
     * @param module               The module being assigned
     * @param totalStudentsAllowed Max students allowed for this module
     * @param groupCount           Number of groups to split into
     * @return List of groups with assigned students
     */
    private void createSessionsForGroups(List<Session> sessions, List<List<Student>> groups, SubjectPlanInfo plan, int groupCount) {

        if (plan.hasLecture()) {
                List<Student> allStudents = groups.stream().flatMap(List::stream).collect(Collectors.toList());
                sessions.addAll(createSessions(allStudents, plan.getMainLecturer(), "Lecture", plan.getSubjectCode() + "-Lecture-G1"));
                System.out.println(" - Created " + allStudents.size() + " Lecture sessions");
            }

        for (int i = 0; i < groupCount; i++) {
            List<Student> groupStudents = groups.get(i);
            String groupSuffix = "-G" + (i + 1);

            if (plan.hasPractical()) {
                sessions.addAll(createSessions(groupStudents, plan.getPracticalTutor(), "Practical", plan.getSubjectCode() + "-Practical" + groupSuffix));
                System.out.println(" - Created " + groupStudents.size() + " Practical sessions");
            }

            if (plan.hasTutorial()) {
                sessions.addAll(createSessions(groupStudents, plan.getTutorialTutor(), "Tutorial", plan.getSubjectCode() + "-Tutorial" + groupSuffix));
                System.out.println(" - Created " + groupStudents.size() + " Tutorial sessions");
            }

            if (plan.hasWorkshop()) {
                sessions.addAll(createSessions(groupStudents, plan.getWorkshopTutor(), "Workshop", plan.getSubjectCode() + "-Workshop" + groupSuffix));
                System.out.println(" - Created " + groupStudents.size() + " Workshop sessions");
            }
        }
    }

    /**
     * Creates sessions for all groups based on the subject plan and session types.
     *
     * @param sessions    List to accumulate created sessions
     * @param groups      List of student groups
     * @param plan        Subject plan containing session info and tutors
     * @param groupCount  Total number of student groups
     */
    private List<Session> createSessions(List<Student> students, String lecturerName, String type, String groupName) {
        List<Session> groupSessions = new ArrayList<>();
        String name = FilterUtil.extractName(lecturerName);
        Optional<Lecturer> lecturer = lecturerService.getLecturerByName(name);

        if (students.isEmpty()) return groupSessions;

        for (Student student : students) {
            Session session = new Session();
            session.setStudent(student);
            session.setType(type);
            session.settype_group(groupName);
            session.setLecturer(lecturer.orElse(null));
            groupSessions.add(session);
            
            System.out.println(session);
        }

        return groupSessions;
    }

    // private void applyConstraintsScheduling(List<Session> sessions){
    //     // 1. Generate domain (valid time slots) for each session
    //     List<TimeSlot> domain = TimeSlotFactory.generateValidTimeSlots();

    //     // 2.  Wrap sessions as CSP variables
    //     List<Variable> variables = sessions.stream()
    //             .map(session -> new Variable(session, new ArrayList<>(domain)))
    //             .collect(Collectors.toList());

    //     // 3. Create a constraint group
    //     ConstraintGroup constraintGroup = new ConstraintGroup();
    //     constraintGroup.addConstraint(new LecturerClashConstraint(variables));
    //     constraintGroup.addConstraint(new StudentClashConstraint(variables));
    //     constraintGroup.addConstraint(new ModuleConflictConstraint(variables));
    //     constraintGroup.addConstraint(new UniqueTypePerWeekConstraint(variables));

    //     // 4. Solve using AC-3 preprocessing and Backtracking
    //     AC3 ac3 = new AC3();
    //     if(!ac3.runAC3(variables, List.of(constraintGroup))) {
    //         throw new IllegalStateException("No valid schedule possible under constraints.");
    //     }

    //     // Create and pass in empty assignment map
    //     Map<Variable, TimeSlot> assignment = new HashMap<>();
    //     BacktrackingSolver solver = new BacktrackingSolver();

    //     boolean success = solver.solve(assignment, variables, List.of(constraintGroup));
    //     if (!success) {
    //         System.out.println("Backtracking failed: No solution found.");
    //         return;
    //     }

    //     // 5. Assign time slot back to sessions
    //     for (Map.Entry<Variable, TimeSlot> entry : assignment.entrySet()) {
    //         Session session = entry.getKey().getSession();
    //         TimeSlot timeSlot = entry.getValue();
    //         session.setDay(timeSlot.getDay().toString());
    //         session.setStartTime(timeSlot.getStartTime());
    //         session.setEndTime(timeSlot.getEndTime());
    //     }

    //     System.out.println("Time slots successfully assigned to sessions");
    // }
}

