package com.sunway.course.timetable.service.processor;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.akka.actor.SessionAssignmentActor;
import com.sunway.course.timetable.akka.actor.VenueCoordinatorActor;
import com.sunway.course.timetable.engine.ConstraintEngine;
import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;
import com.sunway.course.timetable.model.assignment.SessionAssignmentResult;
import com.sunway.course.timetable.model.assignment.SessionGroupMetaData;
import com.sunway.course.timetable.model.plancontent.PlanContent;
import com.sunway.course.timetable.model.plancontent.PlanContentId;
import com.sunway.course.timetable.service.LecturerServiceImpl;
import com.sunway.course.timetable.service.ModuleServiceImpl;
import com.sunway.course.timetable.service.PlanContentServiceImpl;
import com.sunway.course.timetable.service.SessionServiceImpl;
import com.sunway.course.timetable.service.cluster.ProgrammeDistributionClustering;
import com.sunway.course.timetable.service.processor.preprocessing.SessionGroupPreprocessorService;
import com.sunway.course.timetable.service.tracker.CreditHourTracker;
import com.sunway.course.timetable.service.venue.VenueDistanceServiceImpl;
import com.sunway.course.timetable.service.venue.VenueSorterService;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
import com.sunway.course.timetable.singleton.StudentAvailabilityMatrix;
import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;
import com.sunway.course.timetable.util.FilterUtil;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import akka.actor.typed.javadsl.AskPattern;


/**
 * Service class responsible for processing module assignments by:
 * - Grouping students based on intake months.
 * - Allocating students into sessions (lecture, practical, tutorial, workshop).
 * - Enforcing credit hour limits using CreditHourTracker.
 * - Optionally applying constraints for scheduling (currently commented out).
 */

@Service
public class ModuleAssignmentProcessor {
    private static final Logger log = LoggerFactory.getLogger(ModuleAssignmentProcessor.class);
    // private final int MAX_GROUP_SIZE = 35;
    private final CreditHourTracker creditTracker;
    private final LecturerServiceImpl lecturerService;
    private final SessionServiceImpl sessionService;
    private final PlanContentServiceImpl planContentService;
    private final SessionGroupPreprocessorService sessionGroupPreprocessorService;
    private final ModuleServiceImpl moduleService;
    private final ConstraintEngine constraintEngine;
    private final ProgrammeDistributionClustering clustering;
    private final Map<Integer, Map<String, List<Session>>> sessionBySemesterAndModule = new HashMap<>();
    private Map<Long, Integer> studentSemesterMap = new HashMap<>();
    private Map<String, List<Student>> moduleIdToStudentsMap = new HashMap<>();
    private Map<Session, Venue> sessionVenueMap = new HashMap<>();
    private Map<Integer, Map<String, TreeMap<LocalTime, String>>> lastAssignedVenuePerDay = new HashMap<>();
    private Map<Integer, Map<String, Map<String, Double>>> programmeDistribution = new HashMap<>();
    


    // Actor system and singletons for scheduling
    private final ActorSystem<Void> actorSystem;
    private final VenueSorterService venueSorterService;
    private final VenueAvailabilityMatrix venueMatrix;
    private final LecturerAvailabilityMatrix lecturerMatrix;
    private final StudentAvailabilityMatrix studentMatrix;
    private final ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> venueCoordinatorActor;

    @Autowired
    private VenueDistanceServiceImpl venueDistanceService;


    public ModuleAssignmentProcessor(LecturerServiceImpl lecturerService,
                                      ConstraintEngine constraintEngine,
                                      ProgrammeDistributionClustering clustering,
                                      ActorSystem<Void> actorSystem,
                                      VenueSorterService venueSorterService,
                                      VenueAvailabilityMatrix venueMatrix,
                                      LecturerAvailabilityMatrix lecturerMatrix,
                                      StudentAvailabilityMatrix studentMatrix,
                                      ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> venueCoordinatorActor,
                                      SessionServiceImpl sessionService,
                                      PlanContentServiceImpl planContentService,
                                      ModuleServiceImpl moduleService,
                                      SessionGroupPreprocessorService sessionGroupPreprocessorService) {
        this.lecturerService = lecturerService;
        this.constraintEngine = constraintEngine;
        this.clustering = clustering;
        this.actorSystem = actorSystem;
        this.venueSorterService = venueSorterService;
        this.venueMatrix = venueMatrix;
        this.lecturerMatrix = lecturerMatrix;
        this.studentMatrix = studentMatrix;
        this.venueCoordinatorActor = venueCoordinatorActor;
        this.sessionService = sessionService;
        this.planContentService = planContentService;
        this.moduleService = moduleService;
        this.sessionGroupPreprocessorService = sessionGroupPreprocessorService;
        this.creditTracker = new CreditHourTracker();
    }
 
    /**
     * Main entry point to process and assign sessions based on module data.
     * 
     * Later might use Map<Integer, Map<String, Map<String, Double>>> programmeDistribution
     *
     * @param moduleDataList List of ModuleAssignmentData containing module, subject plan, and programmes.
     * @param studentSemesterMap Mapping of student ID to semester
     * @return  Map of semester -> (module ID -> list of generated sessions)
     */
    public Map<Integer, Map<String, List<Session>>> processAssignments(
        List<ModuleAssignmentData> moduleDataList,
        Map<Long, Integer> studentSemesterMap) {

        this.studentSemesterMap = studentSemesterMap;
        // this.programmeDistribution = programmeDistribution;
        sessionBySemesterAndModule.clear();

        List<SessionGroupMetaData> allMetaData = new ArrayList<>();
        Map<SessionGroupMetaData, Module> metaToModuleMap = new HashMap<>();
        

        for (ModuleAssignmentData data : moduleDataList) {
            Module module = data.getModule();
            List<Student> eligibleStudents = creditTracker.filterEligible(
                new ArrayList<>(data.getEligibleStudents()), module.getCreditHour());

            // Cache Students by module ID
            moduleIdToStudentsMap.put(module.getId(), eligibleStudents);

            // Add this after the for-loop
            Set<Student> allStudents = moduleIdToStudentsMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());

            studentMatrix.initializeStudents(new ArrayList<>(allStudents));


            // === 1. Generate SessionGroupMetaData without creating Session objects yet
            List<SessionGroupMetaData> metaList =
                sessionGroupPreprocessorService.prepareSessionGroupMetadata(data, studentSemesterMap);

            allMetaData.addAll(metaList);
            for (SessionGroupMetaData meta : metaList) {
                metaToModuleMap.put(meta, module);
            }
        }

        // === 2. Schedule each session group via actor-based scheduling
        Map<Session, String> sessionToModuleIdMap = new HashMap<>();

        for (SessionGroupMetaData meta : allMetaData) {
            List<Student> assignedStudents;
            // System.out.println("Student List: " + meta.getAssignedStudents() + "Module ID: " + meta.getModuleId() +
            //     "Type: " + meta.getType() + "Group: " + meta.getTypeGroup() +
            //     "Total Students: " + meta.getTotalStudents() + "Lecturer: " + meta.getLecturerName());
            List<Student> eligibleStudents = moduleIdToStudentsMap.get(meta.getModuleId());
            String lecturerName = FilterUtil.extractName(meta.getLecturerName());
            SessionAssignmentResult result = scheduleSessionViaActors(
                meta.getSemester(), meta.getModuleId(), meta.getType(),
                meta.getTypeGroup(), meta.getTotalStudents(), lecturerName,
                eligibleStudents, meta.getGroupIndex(), meta.getGroupCount()
            );

            if (result == null) {
                log.warn("Skipping session group {} due to scheduling failure.", meta.getTypeGroup());
                continue;
            }

            // === 3. Create dummy sessions with scheduled data (students not assigned yet)
            assignedStudents = result.getAssignedStudents();
            // String lecturerName = FilterUtil.extractName(meta.getLecturerName());

            for (Student student : assignedStudents) {
                Session s = new Session();
                s.setType(meta.getType());
                s.setTypeGroup(meta.getTypeGroup());
                s.setLecturer(lecturerService.getLecturerByName(lecturerName).orElse(null));
                s.setDay(result.getDay());
                s.setStartTime(result.getStartTime());
                s.setEndTime(result.getEndTime());
                s.setStudent(student); // per-student assignment

                sessionToModuleIdMap.put(s, meta.getModuleId());
                sessionVenueMap.put(s, result.getVenue()); // Track the assigned venue here
            }
        }

        // === 3. Print or Export Final Schedule
        printFinalizedSchedule(sessionToModuleIdMap, sessionVenueMap);

        // === 4. Persist and group sessions (with placeholder student, to be updated later)
        // persistAndGroupSessions(sessionToModuleIdMap);

        actorSystem.terminate();
        return sessionBySemesterAndModule;
    }

    private void persistAndGroupSessions(Map<Session, String> sessionToModuleIdMap) {
        sessionBySemesterAndModule.clear();

        for (Map.Entry<Session, String> entry : sessionToModuleIdMap.entrySet()) {
            Session session = entry.getKey();
            String moduleId = entry.getValue();

            // Persist session
            Session savedSession = sessionService.saveSession(session);

            // Fetch module by ID
            Optional<Module> optionalModule = moduleService.getModuleById(moduleId);
            if (optionalModule.isEmpty()) {
                log.warn("Module {} not found in DB, skipping PlanContent saving.", moduleId);
                continue;
            }
            Module module = optionalModule.get();

            // Save PlanContent relation
            PlanContent planContent = new PlanContent();
            PlanContentId planContentId = new PlanContentId();
            planContentId.setModuleId(module.getId());
            planContentId.setSessionId(savedSession.getId());
            planContent.setPlanContentId(planContentId);
            planContent.setModule(module);
            planContent.setSession(savedSession);
            planContentService.savePlanContent(planContent);

            // Group by semester and module
            Integer semester = studentSemesterMap.get(session.getStudent().getId());
            if (semester == null) continue;

            sessionBySemesterAndModule
                .computeIfAbsent(semester, k -> new HashMap<>())
                .computeIfAbsent(module.getId(), k -> new ArrayList<>())
                .add(savedSession);
        }
    }

    /**
     * Call the actor model to schedule a session based on type, group and total student count,
     * assigning day/start/end and best-fit venue.
     *
     * @param type         The session type (Lecture, Tutorial, Practical, etc)
     * @param typeGroup    The group key for the session (e.g. MTH1114-Lecture-G1)
     * @param totalStudents Number of students in this group
     * @return SessionAssignmentResult containing day, startTime, endTime, and assigned Venue
     */
    private SessionAssignmentResult scheduleSessionViaActors(int semester,
        String moduleId, String type, String typeGroup, int totalStudents, String lecturerName,
        List<Student> eligibleStudent, int groupIndex, int groupCount) {

        Map<String, Map<String, Double>> semMap = programmeDistribution.getOrDefault(semester, Collections.emptyMap());
        Map<String, Double> progMap = semMap.getOrDefault(moduleId, Collections.emptyMap());
        
        String majorityProgramme = progMap.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("UNKNOWN");

        // Get last assigned venue (if any) for this programme and day
        TreeMap<LocalTime, String> venueTimeMap = lastAssignedVenuePerDay
                .computeIfAbsent(semester, k -> new HashMap<>())
                .computeIfAbsent(majorityProgramme, k -> new TreeMap<>());

        String lastVenue = venueTimeMap.isEmpty() ? null : venueTimeMap.lastEntry().getValue();

        List<String> preferredVenues = lastVenue != null
                ? venueSorterService.findNearestVenues(lastVenue).stream().map(venue -> String.valueOf(venue.getId())).toList()
                : Collections.emptyList();

        int durationHours = getDurationHours(type);
        String actorName = "sessionAssigner-" + typeGroup.replaceAll("\\W+", "");

        Module module = moduleService.getModuleById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + moduleId));

        ActorRef<SessionAssignmentActor.SessionAssignmentCommand> sessionAssigner =
                actorSystem.systemActorOf(SessionAssignmentActor.create(), actorName, Props.empty());

        CompletionStage<SessionAssignmentActor.SessionAssignmentResult> futureResponse = AskPattern.ask(
            sessionAssigner,
            replyTo -> new SessionAssignmentActor.AssignSession(
                    durationHours, totalStudents, lecturerName, 
                    module,
                    eligibleStudent,
                    type,
                    groupIndex,
                    groupCount,
                    venueCoordinatorActor, 
                    replyTo, 
                    preferredVenues
            ),
            Duration.ofSeconds(100),
            actorSystem.scheduler()
        );

        SessionAssignmentActor.SessionAssignmentCommand response = futureResponse.toCompletableFuture().join();

        if (response instanceof SessionAssignmentActor.SessionAssigned assigned) {
            String day = switch (assigned.dayIndex) {
                case 0 -> "Monday";
                case 1 -> "Tuesday";
                case 2 -> "Wednesday";
                case 3 -> "Thursday";
                case 4 -> "Friday";
                default -> throw new IllegalArgumentException("Invalid day index: " + assigned.dayIndex);
            };

            LocalTime start = LocalTime.of(8, 0).plusMinutes(assigned.startIndex * 30L);
            LocalTime end = start.plusMinutes(assigned.durationSlots * 30L);
            return new SessionAssignmentResult(day, start, end, assigned.venue, assigned.getAssignedStudents());
        }

        if (response instanceof SessionAssignmentActor.SessionAssignmentFailed failed) {
            log.warn("Session assignment failed for group {}: {}", typeGroup, failed.reason);
            return null;
        }

        throw new IllegalStateException("Unexpected response from SessionAssignmentActor");
    }


    private void printFinalizedSchedule(Map<Session, String> sessionToModuleIdMap, Map<Session, Venue> sessionVenueMap) {
        Map<String, Map<String, Map<String, Map<String, List<Session>>>>> categorized = new TreeMap<>();

        for (Session s : sessionToModuleIdMap.keySet()) {
            if (s.getStudent() == null) continue;

            String day = s.getDay();
            String module = sessionToModuleIdMap.get(s);
            String type = s.getType();
            String group = s.getTypeGroup();

            categorized
                .computeIfAbsent(day, d -> new TreeMap<>())
                .computeIfAbsent(module, m -> new TreeMap<>())
                .computeIfAbsent(type, t -> new TreeMap<>())
                .computeIfAbsent(group, g -> new ArrayList<>())
                .add(s);
        }

        for (var dayEntry : categorized.entrySet()) {
            System.out.println("===== " + dayEntry.getKey() + " =====");

            for (var moduleEntry : dayEntry.getValue().entrySet()) {
                String module = moduleEntry.getKey();

                for (var typeEntry : moduleEntry.getValue().entrySet()) {
                    String type = typeEntry.getKey();

                    for (var groupEntry : typeEntry.getValue().entrySet()) {
                        String group = groupEntry.getKey();
                        List<Session> sessions = groupEntry.getValue();

                        for (Session s : sessions) {
                            Venue venueObj = sessionVenueMap.get(s);
                            String venue = (venueObj != null) ? venueObj.getName() : "N/A";
                            String lecturer = s.getLecturer() != null ? s.getLecturer().getName() : "N/A";
                            String time = s.getStartTime() + " - " + s.getEndTime();
                            Long studentId = s.getStudent().getId();

                            System.out.printf(
                                "Module: %s | Type: %s | Group: %s | Day: %s | Time: %s | Venue: %s | Lecturer: %s | Student: %d%n",
                                module, type, group, s.getDay(), time, venue, lecturer, studentId
                            );
                        }
                    }
                }
            }
        }
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
    // public Pair<List<Session>, Map<Integer, Map<String, Map<String, Double>>>> clusterProgrammeDistribution(List<ModuleAssignmentData> moduleDataList,
    //                                                                                                         Map<Long, String> studentProgrammeMap,
    //                                                                                                         Map<Long, Integer> studentSemesterMap) {
    //     sessionBySemesterAndModule.clear(); // Reset
        
    //     List<Session> sessions = new ArrayList<>();
    //     for (ModuleAssignmentData data : moduleDataList) {
    //         sessions.addAll(processSingleModuleAssignment(data,studentSemesterMap));
    //     }

    //     List<SessionGroupMetaData> allMetaData = new ArrayList<>();
    //     Map<SessionGroupMetaData, Module> metaToModuleMap = new HashMap<>();

    //     for (ModuleAssignmentData data : moduleDataList) {
    //         Module module = data.getModule();

    //         // === 1. Generate SessionGroupMetaData without creating Session objects yet
    //         List<SessionGroupMetaData> metaList =
    //             sessionGroupPreprocessorService.prepareSessionGroupMetadata(data, studentSemesterMap);

    //         allMetaData.addAll(metaList);
    //         for (SessionGroupMetaData meta : metaList) {
    //             metaToModuleMap.put(meta, module);
    //         }
    //     }

    //     // Compute the percentage distribution of students’ programmes by semester
    //     Map<Integer, Map<String, Map<String, Double>>> distribution =
    //             clustering.calculateProgrammePercentageDistributionBySemester(sessions, studentProgrammeMap, studentSemesterMap);

    //     // Return both sessions and clustering result
    //     return new Pair<>(sessions, distribution);
    // }

    /**
     * Processes a single module assignment, creating sessions based on the subject plan and eligible students.
     *
     * @param data ModuleAssignmentData containing module, subject plan, and eligible students
     * @return List of created sessions for the module
     */
    // private List<SessionGroupMetaData> processSingleModuleAssignment(ModuleAssignmentData data, 
    //                                                     Map<Long, Integer> studentSemesterMap) {
    //     SubjectPlanInfo plan = data.getSubjectPlanInfo();
    //     Module module = data.getModule();
    //     String moduleId = module.getId();
    //     Set<Student> eligibleStudents = data.getEligibleStudents();
    //     List<Student> filteredStudents = creditTracker.filterEligible(new ArrayList<>(eligibleStudents), module.getCreditHour());

    //     int groupCount = splitGroup(filteredStudents.size());

    //     return sessionGroupPreprocessorService.prepareSessionGroupMetadata(data, studentSemesterMap);
    // }


    /**
     * Calculates the number of groups required based on total allowed students and max group size.
     *
     * @param totalStudentsAllowed Max number of students allowed to attend the session
     * @return Number of student groups required
     */
    // private int splitGroup(int totalStudentsAllowed) {
    //     return (int) Math.ceil((double) totalStudentsAllowed / MAX_GROUP_SIZE);
    // }


    /**
     * Groups sessions by semester, module, and type+group key.
     *
     * @return Map of semester -> (module ID -> (type+group key -> list of sessions))
     */
    // private Map<Integer, Map<String, Map<String, List<Session>>>> groupSessionsByTypeGroup() {
    //     Map<Integer, Map<String, Map<String, List<Session>>>> grouped = new HashMap<>();
    //     for (var semEntry : sessionBySemesterAndModule.entrySet()) {
    //         Integer semester = semEntry.getKey();
    //         for (var modEntry : semEntry.getValue().entrySet()) {
    //             String moduleId = modEntry.getKey();
    //             for (Session session : modEntry.getValue()) {
    //                 String key = session.getType() + "||" + session.getTypeGroup();
    //                 grouped
    //                     .computeIfAbsent(semester, k -> new HashMap<>())
    //                     .computeIfAbsent(moduleId, k -> new HashMap<>())
    //                     .computeIfAbsent(key, k -> new ArrayList<>())
    //                     .add(session);
    //             }
    //         }
    //     }
    //     return grouped;
    // }


    /**
     *  Schedules sessions by iterating through the grouped sessions,
     * @param grouped  Map of semester -> (module ID -> (type+group key -> list of sessions))
     */
    // private void scheduleSessions(Map<Integer, Map<String, Map<String, List<Session>>>> grouped) {
    //     for (var semesterEntry : grouped.entrySet()) {
    //         int semester = semesterEntry.getKey();
    //         for (var moduleEntry : semesterEntry.getValue().entrySet()) {
    //             String moduleEntryId = moduleEntry.getKey();
    //             for (var tgEntry : moduleEntry.getValue().entrySet()) {
    //                 List<Session> sessions = tgEntry.getValue();
    //                 if (sessions.isEmpty()) continue;

    //                 Session sample = sessions.get(0);
    //                 String lecturerId = sample.getLecturer().getId().toString();

    //                 SessionAssignmentResult result = scheduleSessionViaActors(
    //                     semester, moduleEntryId, sample.getType(), sample.getTypeGroup(), sessions.size(), lecturerId);

    //                 if (result == null) {
    //                     log.warn("Skipping session group {} due to scheduling failure.", tgEntry.getKey());
    //                     continue;
    //                 }

    //                 for (Session session : sessions) {
    //                     session.setDay(result.getDay());
    //                     session.setStartTime(result.getStartTime());
    //                     session.setEndTime(result.getEndTime());


    //                     // System.out.println( "Scheduled session " + session);
    //                     Session savedSession = sessionService.saveSession(session); // Persist session

    //                     String moduleId = moduleEntry.getKey();
    //                     Optional<Module> optionalModule = moduleService.getModuleById(moduleId);
    //                     if (optionalModule.isEmpty()) {
    //                         log.warn("Module {} not found in DB, skipping PlanContent saving.", moduleId);
    //                         continue;
    //                     }
    //                     Module module = optionalModule.get();

    //                     // Save plan_content
    //                     PlanContent planContent = new PlanContent();
    //                     PlanContentId planContentId = new PlanContentId();
    //                     planContentId.setModuleId(module.getId());
    //                     planContentId.setSessionId(savedSession.getId());
    //                     planContent.setPlanContentId(planContentId);
    //                     planContent.setModule(module);
    //                     planContent.setSession(savedSession);

    //                     planContentService.savePlanContent(planContent); // Persist to plan_content


    //                     // For debug, print assigned session details:
    //                     System.out.printf("Assigned session: Semester %d, Module %s, Type %s, Group %s, Day %s, Start %s, End %s, Venue %s Lecturer%s Student %d%n",
    //                         semester,
    //                         moduleEntryId,
    //                         session.getType(),
    //                         session.getTypeGroup(),
    //                         session.getDay(),
    //                         session.getStartTime(),
    //                         session.getEndTime(),
    //                         result.getVenue().getName(),
    //                         session.getLecturer().getName()
    //                         // session.getStudent().getId()
    //                     );
    //                 }
    //             }
    //         }
    //     }
    // }

    /**
     *  Assigns students to groups based on the total number of students and the number of groups.
     * 
     * @param students 
     * @param module
     * @param totalStudents
     * @param groupCount
     * @return
     */
    // private List<List<Student>> assignStudentsToGroups(
    //         List<Student> students, 
    //         Module module,
    //         int totalStudents,
    //         int groupCount) {

    //     List<List<Student>> groups = new ArrayList<>();
    //     for (int i = 0; i < groupCount; i++) {
    //         groups.add(new ArrayList<>());
    //     }


    //     int totalAssigned = 0;
    //     int currentGroupIndex = 0;

    //      for (Student student : students) {
    //         if (totalAssigned >= totalStudents) {
    //             break;
    //         }
    //         creditTracker.deductCredits(student, module.getCreditHour());

    //         groups.get(currentGroupIndex).add(student);
    //         totalAssigned++;
    //         currentGroupIndex = (currentGroupIndex + 1) % groupCount;
    //     }

    //     return groups;
    // }

    /**
     *  Creates sessions for each group based on the subject plan and session types.
     *  Creates a lecture session for all students, then creates tutorial, practical, and workshop sessions for each group.
     *   This method handles the case where there is only one group or no tutors available.
     *   If there are multiple groups, it creates sessions for each group with the respective tutor.
     *   This method also handles the case where there are no students in a group.
     * 
     *   @param sessions List to accumulate created sessions
     *   @param groups List of student groups
     *   @param plan Subject plan containing session info and tutors
     *   @param groupCount Total number of student groups
     */
    // private void createSessionsForGroups(List<Session> sessions, List<List<Student>> groups, SubjectPlanInfo plan, int groupCount) {

    //     if (plan.hasLecture()) {
    //             List<Student> allStudents = groups.stream().flatMap(List::stream).collect(Collectors.toList());
    //             sessions.addAll(createSessions(allStudents, plan.getMainLecturer(), "Lecture", plan.getSubjectCode() + "-Lecture-G1"));
    //             // System.out.println(" - Created " + allStudents.size() + " Lecture sessions");
    //         }

    //     for (int i = 0; i < groupCount; i++) {
    //         List<Student> groupStudents = groups.get(i);
    //         String groupSuffix = "-G" + (i + 1);
    //         List<SessionTypeInfo> sessionTypes = List.of(
    //             new SessionTypeInfo("Tutorial", plan.hasTutorial(), plan.getTutorialTutor()),
    //             new SessionTypeInfo("Practical", plan.hasPractical(), plan.getPracticalTutor()),
    //             new SessionTypeInfo("Workshop", plan.hasWorkshop(), plan.getWorkshopTutor())
    //         );

    //         for (SessionTypeInfo typeInfo : sessionTypes) {
    //             if(!typeInfo.hasType) continue;

    //             List<String> lecturerNames = typeInfo.tutor; // e.g., ["John", "Mary"]

    //             // One group or no tutors available
    //             if (groupCount == 1 || lecturerNames.isEmpty()) {
    //                 String firstLecturer = lecturerNames.isEmpty() ? null : lecturerNames.get(0);
    //                 String sessionCode = plan.getSubjectCode() + "-" + typeInfo.type + groupSuffix;
    //                 sessions.addAll(createSessions(groupStudents, firstLecturer, typeInfo.type, sessionCode));
    //             } else {
    //                 // Multiple groups → distribute lecturers round-robin
    //                 String selectedLecturer = lecturerNames.get(i % lecturerNames.size());
    //                 String sessionCode = plan.getSubjectCode() + "-" + typeInfo.type + groupSuffix;
    //                 sessions.addAll(createSessions(groupStudents, selectedLecturer, typeInfo.type, sessionCode));
    //             }

    //             // String sessionCode = plan.getSubjectCode() + "-" + typeInfo.type + groupSuffix;
    //             // List<Session> groupSessions = createSessions(groupStudents, typeInfo.tutor, typeInfo.type, sessionCode);
    //             // sessions.addAll(groupSessions);
    //             // System.out.printf(" - Created %d %s sessions\n", groupSessions.size(), typeInfo.type());
    //         }
    //     }
    // }

    // /**
    //  * Creates sessions for all groups based on the subject plan and session types.
    //  *
    //  * @param sessions    List to accumulate created sessions
    //  * @param groups      List of student groups
    //  * @param plan        Subject plan containing session info and tutors
    //  * @param groupCount  Total number of student groups
    //  */
    // private List<Session> createSessions(List<Student> students, String lecturerName, String type, String groupName) {
    //     List<Session> groupSessions = new ArrayList<>();
    //     String name = FilterUtil.extractName(lecturerName);
    //     Optional<Lecturer> lecturer = lecturerService.getLecturerByName(name);

    //     if (students.isEmpty()) return groupSessions;

    //     for (Student student : students) {
    //         Session session = new Session();
    //         session.setStudent(student);
    //         session.setType(type);
    //         session.setTypeGroup(groupName);
    //         session.setLecturer(lecturer.orElse(null));
    //         groupSessions.add(session);
            
    //     }

    //     return groupSessions;
    // }

    // private static record SessionTypeInfo(String type, boolean hasType, List<String> tutor) {}

    private int getDurationHours(String type) {
        return switch (type.toUpperCase()) {
            case "LECTURE" -> 2;
            case "TUTORIAL" -> 2;
            case "WORKSHOP" -> 2;
            case "PRACTICAL" -> 2;
            default -> 1;
        };
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



