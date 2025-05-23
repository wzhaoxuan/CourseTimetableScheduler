package com.sunway.course.timetable.service.processor;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sunway.course.timetable.engine.ConstraintEngine;
import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.SubjectPlanInfo;
import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;
import com.sunway.course.timetable.model.programme.Programme;
import com.sunway.course.timetable.service.LecturerServiceImpl;
import com.sunway.course.timetable.service.tracker.CreditHourTracker;
import com.sunway.course.timetable.util.DateUtil;
import com.sunway.course.timetable.util.FilterUtil;


public class ModuleAssignmentProcessor {

    private final int MAX_GROUP_SIZE = 35;
    private final List<ModuleAssignmentData> moduleDataList;
    private final CreditHourTracker creditTracker;
    private final LecturerServiceImpl lecturerService;

    public ModuleAssignmentProcessor(List<ModuleAssignmentData> moduleDataList,
                                      LecturerServiceImpl lecturerService) {
        this.moduleDataList = moduleDataList;
        this.lecturerService = lecturerService;
        this.creditTracker = new CreditHourTracker();
    }

    public List<Session> processAssignments() {
        List<Session> sessions = new ArrayList<>();

        for(ModuleAssignmentData data : moduleDataList) {
            SubjectPlanInfo plan = data.getSubjectPlanInfo();
            Module module = data.getModule();
            List<Programme> programmes = data.getProgrammeOfferingModules();

            // System.out.println("Processing module: " + module.getName() + " (" + module.getId() + ")");

            Map<Month, List<Student>> studentsByIntake = groupStudentsByIntake(programmes);
            printGroupedStudents(studentsByIntake);

            int totalStudentsAllowed = plan.getTotalStudents();
            int groupCount = calculateGroupCount(totalStudentsAllowed);

            List<List<Student>> groups = assignStudentsToGroups(studentsByIntake, module, totalStudentsAllowed, groupCount);
            createSessionsForGroups(sessions, groups, plan, groupCount);
        }

        System.out.println("Total sessions created: " + sessions.size());

        // Apply constraints to the sessions
        ConstraintEngine constraintEngine = new ConstraintEngine();
        System.out.println("Engine started");
        constraintEngine.scheduleSessions(sessions);
        
        return sessions;
    }

    private Map<Month, List<Student>> groupStudentsByIntake(List<Programme> programmes) {
        return programmes.stream()
                .collect(Collectors.groupingBy(
                        p -> DateUtil.parseMonth(p.getIntake()),
                        Collectors.mapping(Programme::getStudent, Collectors.toList())
                ));
    }

    private void printGroupedStudents(Map<Month, List<Student>> studentsByIntake) {
        // System.out.println("Grouped students by intake month:");
        for (Map.Entry<Month, List<Student>> entry : studentsByIntake.entrySet()) {
            // System.out.println(" - " + entry.getKey() + ": " + entry.getValue().size() + " students");
        }
    }

    private int calculateGroupCount(int totalStudentsAllowed) {
        return (int) Math.ceil((double) totalStudentsAllowed / MAX_GROUP_SIZE);
    }

    private List<List<Student>> assignStudentsToGroups(
            Map<Month, List<Student>> studentsByIntake,
            Module module,
            int totalStudentsAllowed,
            int groupCount) {

        List<List<Student>> groups = new ArrayList<>();
        for (int i = 0; i < groupCount; i++) {
            groups.add(new ArrayList<>());
        }

        int totalAssigned = 0;
        int currentGroupIndex = 0;

        for (Map.Entry<Month, List<Student>> intakeEntry : studentsByIntake.entrySet()) {
            List<Student> batchStudents = intakeEntry.getValue();
            List<Student> eligibleStudents = creditTracker.filterEligible(batchStudents, module.getCreditHour());

            for (Student student : eligibleStudents) {
                if (totalAssigned >= totalStudentsAllowed) break;

                creditTracker.deductCredits(student, module.getCreditHour());

                groups.get(currentGroupIndex).add(student);
                totalAssigned++;

                currentGroupIndex = (currentGroupIndex + 1) % groupCount;
            }

            // System.out.println("Assigned students so far: " + totalAssigned);
            if (totalAssigned >= totalStudentsAllowed) {
                // System.out.println("Reached total students limit (" + totalStudentsAllowed + ") for module " + module.getName());
                break;
            }
        }
        return groups;
    }

    private void createSessionsForGroups(List<Session> sessions, List<List<Student>> groups, SubjectPlanInfo plan, int groupCount) {

        if (plan.hasLecture()) {
                List<Student> allStudents = groups.stream().flatMap(List::stream).collect(Collectors.toList());
                sessions.addAll(createSessions(allStudents, plan.getMainLecturer(), "Lecture", plan.getSubjectCode() + "-Lecture-G1"));
                // System.out.println(" - Created " + allStudents.size() + " Lecture sessions");
            }

        for (int i = 0; i < groupCount; i++) {
            List<Student> groupStudents = groups.get(i);
            String groupSuffix = "-G" + (i + 1);

            if (plan.hasPractical()) {
                sessions.addAll(createSessions(groupStudents, plan.getPracticalTutor(), "Practical", plan.getSubjectCode() + "-Practical" + groupSuffix));
                // System.out.println(" - Created " + groupStudents.size() + " Practical sessions");
            }

            if (plan.hasTutorial()) {
                sessions.addAll(createSessions(groupStudents, plan.getTutorialTutor(), "Tutorial", plan.getSubjectCode() + "-Tutorial" + groupSuffix));
                // System.out.println(" - Created " + groupStudents.size() + " Tutorial sessions");
            }

            if (plan.hasWorkshop()) {
                sessions.addAll(createSessions(groupStudents, plan.getWorkshopTutor(), "Workshop", plan.getSubjectCode() + "-Workshop" + groupSuffix));
                // System.out.println(" - Created " + groupStudents.size() + " Workshop sessions");
            }
        }
    }

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

