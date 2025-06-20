package com.sunway.course.timetable.service.processor;
import java.io.File;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunway.course.timetable.akka.actor.SessionAssignmentActor;
import com.sunway.course.timetable.akka.actor.VenueCoordinatorActor;
import com.sunway.course.timetable.engine.BacktrackingScheduler;
import com.sunway.course.timetable.engine.DomainPruner.AssignmentOption;
import com.sunway.course.timetable.evaluator.FitnessEvaluator;
import com.sunway.course.timetable.evaluator.FitnessResult;
import com.sunway.course.timetable.exporter.TimetableExcelExporter;
import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Satisfaction;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;
import com.sunway.course.timetable.model.assignment.SessionGroupMetaData;
import com.sunway.course.timetable.model.plan.Plan;
import com.sunway.course.timetable.model.plan.PlanId;
import com.sunway.course.timetable.model.plancontent.PlanContent;
import com.sunway.course.timetable.model.plancontent.PlanContentId;
import com.sunway.course.timetable.model.venueAssignment.VenueAssignment;
import com.sunway.course.timetable.model.venueAssignment.VenueAssignmentId;
import com.sunway.course.timetable.result.FinalAssignmentResult;
import com.sunway.course.timetable.result.SessionAssignmentResult;
import com.sunway.course.timetable.service.LecturerServiceImpl;
import com.sunway.course.timetable.service.ModuleServiceImpl;
import com.sunway.course.timetable.service.PlanContentServiceImpl;
import com.sunway.course.timetable.service.PlanServiceImpl;
import com.sunway.course.timetable.service.SatisfactionServiceImpl;
import com.sunway.course.timetable.service.SessionServiceImpl;
import com.sunway.course.timetable.service.cluster.ProgrammeDistributionClustering;
import com.sunway.course.timetable.service.processor.preprocessing.SessionGroupPreprocessorService;
import com.sunway.course.timetable.service.venue.VenueAssignmentServiceImpl;
import com.sunway.course.timetable.service.venue.VenueDistanceServiceImpl;
import com.sunway.course.timetable.service.venue.VenueSorterService;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
import com.sunway.course.timetable.singleton.StudentAvailabilityMatrix;
import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;
import com.sunway.course.timetable.util.FilterUtil;
import com.sunway.course.timetable.util.LecturerDayAvailabilityUtil;
import com.sunway.course.timetable.util.tracker.CreditHourTracker;

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
    private static final double FITNESS_THRESHOLD = 85.0;

    // === Dependencies ===
    private final CreditHourTracker creditTracker;
    private final LecturerServiceImpl lecturerService;
    private final SessionServiceImpl sessionService;
    private final PlanContentServiceImpl planContentService;
    private final VenueDistanceServiceImpl venueDistanceService;
    private final VenueAssignmentServiceImpl venueAssignmentService;
    private final ModuleServiceImpl moduleService;
    private final PlanServiceImpl planService;
    private final SatisfactionServiceImpl satisfactionService;
    private final ProgrammeDistributionClustering clustering;
    private final SessionGroupPreprocessorService sessionGroupPreprocessorService;
    private final VenueSorterService venueSorterService;
    private final TimetableExcelExporter timetableExcelExporter;
    public final LecturerDayAvailabilityUtil lecturerDayAvailabilityUtil;
    public final FitnessEvaluator fitnessEvaluator;


    // === Singleton Matrices ===
    private final VenueAvailabilityMatrix venueMatrix;
    private final LecturerAvailabilityMatrix lecturerMatrix;
    private final StudentAvailabilityMatrix studentMatrix;

    // === Actor===
    private final ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> venueCoordinatorActor;
    private final ActorSystem<Void> actorSystem;

    // === Data Structures ===
    private Map<Session, Venue> sessionVenueMap = new HashMap<>();
    private Map<Long, Integer> studentSemesterMap = new HashMap<>();
    private Map<Session, String> sessionToModuleIdMap = new HashMap<>(); // Tracks which module ID each individual Session belongs to
    private final Map<String, Integer> lecturerTeachingHours = new HashMap<>();
    private final Map<Long, Set<String>> studentAssignedTypes = new HashMap<>();
    private Map<String, List<Student>> moduleIdToStudentsMap = new HashMap<>();
    private Map<Integer, Map<String, Map<String, Double>>> programmeDistribution = new HashMap<>();
    private final Map<Integer, Map<String, List<Session>>> sessionBySemesterAndModule = new HashMap<>(); // Store all scheduled sessions grouped by semester and module ID
    private Map<Integer, Map<String, TreeMap<LocalTime, String>>> lastAssignedVenuePerDay = new HashMap<>();
    private Map<String, SessionAssignmentActor.SessionAssigned> lectureAssignmentsByModule = new HashMap<>();
    
    private double finalScore;
    private List<File> exportedFiles;
    private List<File> exportedLecturerFiles;
    private List<File> exportedModuleFiles;

    public static String CURRENT_VERSION_TAG;

    public ModuleAssignmentProcessor(LecturerServiceImpl lecturerService,
                                     ModuleServiceImpl moduleService,
                                      SessionServiceImpl sessionService,
                                      PlanContentServiceImpl planContentService,
                                      VenueDistanceServiceImpl venueDistanceService,
                                      VenueAssignmentServiceImpl venueAssignmentService,
                                      VenueSorterService venueSorterService,
                                      PlanServiceImpl planService,
                                      SatisfactionServiceImpl satisfactionService,
                                      SessionGroupPreprocessorService sessionGroupPreprocessorService,
                                      VenueAvailabilityMatrix venueMatrix,
                                      LecturerAvailabilityMatrix lecturerMatrix,
                                      StudentAvailabilityMatrix studentMatrix,
                                      ActorSystem<Void> actorSystem,
                                      ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> venueCoordinatorActor,
                                      ProgrammeDistributionClustering clustering,
                                      TimetableExcelExporter timetableExcelExporter,
                                      LecturerDayAvailabilityUtil lecturerDayAvailabilityUtil,
                                      FitnessEvaluator fitnessEvaluator
                                      ) {
        this.lecturerService = lecturerService;
        this.moduleService = moduleService;
        this.sessionService = sessionService;
        this.planContentService = planContentService;
        this.venueDistanceService = venueDistanceService;
        this.venueAssignmentService = venueAssignmentService;
        this.venueSorterService = venueSorterService;
        this.sessionGroupPreprocessorService = sessionGroupPreprocessorService;
        this.planService = planService;
        this.satisfactionService = satisfactionService;
        this.venueMatrix = venueMatrix;
        this.lecturerMatrix = lecturerMatrix;
        this.studentMatrix = studentMatrix;
        this.actorSystem = actorSystem;
        this.venueCoordinatorActor = venueCoordinatorActor;
        this.clustering = clustering;
        this.timetableExcelExporter = timetableExcelExporter;
        this.lecturerDayAvailabilityUtil = lecturerDayAvailabilityUtil;
        this.fitnessEvaluator = fitnessEvaluator;
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
    public FinalAssignmentResult processAssignments(
        List<ModuleAssignmentData> moduleDataList,
        Map<Long, String> studentProgrammeMap,
        Map<Long, Integer> studentSemesterMap,
        String programme,
        String intake,
        int year) {
        
        String versionTag = satisfactionService.getNextVersionTag();
        CURRENT_VERSION_TAG = versionTag;

        this.studentSemesterMap = studentSemesterMap;
        resetState(); // Reset all internal state before processing new assignments

        long startTime = System.currentTimeMillis();

        // 1. Prepare student availability
        Set<Student> allStudents = moduleDataList.stream()
            .flatMap(data -> data.getEligibleStudents().stream())
            .collect(Collectors.toSet());
        studentMatrix.initializeStudents(new ArrayList<>(allStudents));

        // 2. Prepare metadata
        List<SessionGroupMetaData> allMetaData = new ArrayList<>();
        for (ModuleAssignmentData data : moduleDataList) {
            Module module = data.getModule();
            List<Student> eligibleStudents = creditTracker.filterEligible(
                new ArrayList<>(data.getEligibleStudents()), module.getCreditHour());
            moduleIdToStudentsMap.put(module.getId(), eligibleStudents);

            List<SessionGroupMetaData> metaList =
                sessionGroupPreprocessorService.prepareSessionGroupMetadata(data, studentSemesterMap);
            allMetaData.addAll(metaList);
        }

        // 3. Compute majority programme distribution
        var distResult = clustering.clusterProgrammeDistribution(
            allMetaData, moduleIdToStudentsMap, studentProgrammeMap, studentSemesterMap
        );
        this.programmeDistribution = distResult.percentageMap();
        Map<SessionGroupMetaData, String> majorityMap = distResult.majorityProgrammeByGroup();

        // 4. Sort session groups: semester → programme → type
        allMetaData.sort(Comparator
            .comparingInt(SessionGroupMetaData::getSemester)
            .thenComparing(meta -> majorityMap.getOrDefault(meta, "ZZZ"))
            .thenComparing(meta -> switch (meta.getType().toLowerCase()) {
                case "lecture" -> 0;
                case "practical" -> 1;
                case "tutorial" -> 2;
                default -> 3;
            }));


        for (SessionGroupMetaData meta : allMetaData) {
            meta.setEligibleStudents(moduleIdToStudentsMap.get(meta.getModuleId()));
        }

        log.info("Running hybrid scheduling: actor-first, fallback to AC3/backtracking if low fitness");

        processAssignmentsHybrid(allMetaData, programme, intake, year, versionTag);

        long endTime = System.currentTimeMillis(); // End measuring time
        long durationMs = endTime - startTime;

        log.info("Scheduling process completed in {} ms ({} seconds)", durationMs, durationMs / 1000.0);

        log.info("Actor system terminated. Finalizing scheduling...");
        return new FinalAssignmentResult(sessionBySemesterAndModule, exportedFiles, exportedLecturerFiles, exportedModuleFiles, finalScore);
    }


    /**
     * Hybrid Scheduling
     * 1. Attempt actor-based scheduling for each SessionGroupMetaData
     * 2. If any session fails, collect the remaining unshceduled ones
     * 3. Run backtracking only on those that failed.
     * 
     * @param allMetaData
     */
    private void processAssignmentsHybrid(List<SessionGroupMetaData> allMetaData, String programme, String intake, int year, String versionTag) {
        log.info("Running hybrid scheduling: actor-first, fallback to AC3/backtracking if low fitness/fail");

        long actorStart = System.currentTimeMillis();

        List<SessionGroupMetaData> failedMeta = new ArrayList<>();

        for (SessionGroupMetaData meta : allMetaData) {
            List<Student> eligibleStudents = moduleIdToStudentsMap.get(meta.getModuleId());
            String lecturerName = FilterUtil.extractName(meta.getLecturerName());
            List<Student> filteredEligibleStudents = eligibleStudents.stream()
                .filter(s -> {
                    String key = meta.getModuleId() + "-" + meta.getType().toUpperCase();
                    return !studentAssignedTypes.getOrDefault(s.getId(), Set.of()).contains(key);
                })
                .toList();

            SessionAssignmentResult result = scheduleSessionViaActors(
                meta.getSemester(), meta.getModuleId(), meta.getType(), meta.getTypeGroup(),
                meta.getTotalStudents(), lecturerName, filteredEligibleStudents,
                meta.getGroupIndex(), meta.getGroupCount(), lectureAssignmentsByModule
            );

            if (result == null) {
                log.warn("[HYBRID] Actor scheduling failed for {}", meta.getTypeGroup());
                failedMeta.add(meta);
                continue;
            }

            // Reconstruct SessionAssigned and store if type is Lecture
            if (meta.getType().equalsIgnoreCase("Lecture")) {
                int dayIndex = getDayIndex(result.getDay());
                int startSlot = getStartSlot(result.getStartTime());
                int durationSlots = (int) (Duration.between(result.getStartTime(), result.getEndTime()).toMinutes() / 30);

                SessionAssignmentActor.SessionAssigned assigned = new SessionAssignmentActor.SessionAssigned(
                    result.getVenue(), dayIndex, startSlot, durationSlots, result.getAssignedStudents()
                );

                lectureAssignmentsByModule.put(meta.getModuleId(), assigned);
            }

            Lecturer lecturer = lecturerService.getLecturerByName(lecturerName).orElse(null);
            assignStudentsToSession(meta, result.getDay(), result.getStartTime(), result.getEndTime(),
                    lecturer, result.getVenue(), result.getAssignedStudents());
        }

        long actorEnd = System.currentTimeMillis();
        log.info("🎭 Actor-based scheduling completed in {} ms", (actorEnd - actorStart));

        List<Session> allScheduledSessions = sessionBySemesterAndModule.values().stream()
            .flatMap(m -> m.values().stream())
            .flatMap(List::stream)
            .toList();

        // FitnessResult initialFitness = fitnessEvaluator.evaluate(allScheduledSessions, sessionVenueMap);
        // double initialScore = initialFitness.getPercentage();
        // log.info("Initial fitness score after actor-based scheduling: {}%", initialScore);

        if (!failedMeta.isEmpty()) {
            log.info("[HYBRID] Fallback triggered. Using backtracking to improve fitness.");
            long backtrackStart = System.currentTimeMillis();
            log.info("[HYBRID] Fallback triggered. Using backtracking to improve fitness.");
            scheduleWithBacktracking(failedMeta, versionTag);
            long backtrackEnd = System.currentTimeMillis();
            log.info(" Backtracking scheduling completed in {} ms", (backtrackEnd - backtrackStart));
        }
        List<Session> persistedSessions = sessionBySemesterAndModule.values().stream()
        .flatMap(m -> m.values().stream())
        .flatMap(List::stream)
        .toList();

        log.info("Total sessions created: {}", sessionToModuleIdMap.size());
        printFinalizedSchedule(sessionToModuleIdMap, sessionVenueMap);

        FitnessResult finalFitness = fitnessEvaluator.evaluate(persistedSessions, sessionVenueMap, versionTag);
        this.finalScore = finalFitness.getPercentage();
        log.info("Final fitness score after hybrid scheduling: {}%", finalScore);
        
        long persistStart = System.currentTimeMillis();
        persistAndGroupSessions(sessionToModuleIdMap);
        long persistEnd = System.currentTimeMillis();
        log.info(" Session persistence completed in {} ms", (persistEnd - persistStart));

        this.exportedFiles = exportPersistedTimetable(programme, intake, year, finalScore);
        Set<String> allLecturerNames = sessionBySemesterAndModule.values().stream()
            .flatMap(moduleMap -> moduleMap.values().stream())
            .flatMap(List::stream)
            .map(s -> s.getLecturer().getName())
            .filter(Objects::nonNull)
            .sorted()
            .collect(Collectors.toSet());

        this.exportedLecturerFiles = new ArrayList<>();
        for (String lecturerName : allLecturerNames) {
            Map<Integer, List<Session>> lecturerSessions = timetableExcelExporter.filterSessionsByLecturer(
                sessionBySemesterAndModule, lecturerName
            );

            List<File> files = timetableExcelExporter.exportLecturerTimetable(
                lecturerSessions, lecturerName, intake, year
            );
            this.exportedLecturerFiles.addAll(files);
        }

        this.exportedModuleFiles = timetableExcelExporter.exportModuleTimetable(
            sessionBySemesterAndModule, intake, year
        );
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
        List<Student> eligibleStudent, int groupIndex, int groupCount, Map<String, SessionAssignmentActor.SessionAssigned> lectureAssignmentsByModule) {

        Map<String, Map<String, Double>> semMap = programmeDistribution.getOrDefault(semester, Collections.emptyMap());
        Map<String, Double> progMap = semMap.getOrDefault(moduleId, Collections.emptyMap());

        String majorityProgramme = progMap.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("UNKNOWN");

        TreeMap<LocalTime, String> venueTimeMap = lastAssignedVenuePerDay
                .computeIfAbsent(semester, k -> new HashMap<>())
                .computeIfAbsent(majorityProgramme, k -> new TreeMap<>());

        String lastVenue = venueTimeMap.isEmpty()
            ? venueMatrix.getSortedVenues().stream().findFirst().map(Venue::getName).orElse(null)
            : venueTimeMap.lastEntry().getValue();


        System.out.printf("Last venue for %s in semester %d: %s%n", typeGroup, semester, lastVenue);

        List<String> preferredVenues = lastVenue != null
            ? venueSorterService.findNearestVenues(lastVenue).stream()
                .sorted(Comparator.comparingDouble(v -> venueDistanceService.getDistanceScore(lastVenue, v.getName())))
                .map(v -> String.valueOf(v.getId()))
                .toList()
            : Collections.emptyList();

        int durationHours = getDurationHours(type);
        String actorName = String.format("sessionAssigner-%s-%s-%d-%s",
                moduleId.replaceAll("\\W+", ""),
                typeGroup.replaceAll("\\W+", ""),
                semester,
                UUID.randomUUID());


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
                    preferredVenues,
                    lecturerMatrix,
                    venueMatrix,
                    studentMatrix,
                    venueMatrix.getSortedVenues(),
                    lecturerService,
                    lecturerDayAvailabilityUtil,
                    lectureAssignmentsByModule
            ),
            Duration.ofSeconds(100),
            actorSystem.scheduler()
        );

        SessionAssignmentActor.SessionAssignmentCommand response = futureResponse.toCompletableFuture().join();

        if (response instanceof SessionAssignmentActor.SessionAssigned assigned) {
            String day = getDayName(assigned.dayIndex);

            LocalTime start = LocalTime.of(8, 0).plusMinutes(assigned.startIndex * 30L);
            LocalTime end = start.plusMinutes(assigned.durationSlots * 30L);

            // Update preferred venue log
            venueTimeMap.put(start, assigned.venue.getName());

            return new SessionAssignmentResult(day, start, end, assigned.venue, assigned.getAssignedStudents());
        }

        if (response instanceof SessionAssignmentActor.SessionAssignmentFailed failed) {
            log.warn("Session assignment failed for group {}: {}", typeGroup, failed.reason);
            return null;
        }

        throw new IllegalStateException("Unexpected response from SessionAssignmentActor");
    }

    private void scheduleWithBacktracking(List<SessionGroupMetaData> failedMeta, String versionTag) {
        log.info("Starting backtracking scheduling for {} session groups", failedMeta.size());

        List<Venue> allVenues = venueMatrix.getSortedVenues();
        BacktrackingScheduler scheduler = new BacktrackingScheduler(
            failedMeta, lecturerMatrix, venueMatrix, studentMatrix, allVenues, 
            venueDistanceService, lecturerService, lecturerDayAvailabilityUtil,
            fitnessEvaluator
        );
        

        Map<SessionGroupMetaData, AssignmentOption> result = scheduler.solve(versionTag);
        Map<SessionGroupMetaData, List<Student>> studentAssignments = scheduler.getStudentAssignments();

        // UnassignedStudentResolver resolver = new UnassignedStudentResolver();
        // List<SessionGroupMetaData> extraGroups = resolver.resolve(failedMeta, studentAssignments);

        // if (!extraGroups.isEmpty()) {
        //     log.info("Injecting {} additional groups into scheduler", extraGroups.size());
            
        //     // You can rerun your actor or backtracking on these extra groups
        //     scheduleWithBacktracking(extraGroups);
        // }

        // log.info("Checking for unassigned students:");

        // for (SessionGroupMetaData meta : allMetaData) {
        //     List<Student> eligible = meta.getEligibleStudents();
        //     List<Student> assigned = studentAssignments.getOrDefault(meta, List.of());

        //     Set<Long> assignedIds = assigned.stream()
        //         .map(Student::getId)
        //         .collect(Collectors.toSet());

        //     for (Student s : eligible) {
        //         if (!assignedIds.contains(s.getId())) {
        //             log.warn("Student {} not assigned to group {}", s.getId(), meta.getTypeGroup());
        //         }
        //     }
        // }


        for (Map.Entry<SessionGroupMetaData, AssignmentOption> entry : result.entrySet()) {
            SessionGroupMetaData meta = entry.getKey();
            AssignmentOption opt = entry.getValue();

            String lecturerName = FilterUtil.extractName(meta.getLecturerName());
            Optional<Lecturer> lecturerOpt = lecturerService.getLecturerByName(lecturerName);
            if (lecturerOpt.isEmpty()) {
                log.warn("Lecturer {} not found in DB, skipping session creation.", lecturerName);
                continue;
            }

            Lecturer lecturer = lecturerOpt.get();
            String day = getDayName(opt.day());

            LocalTime start = LocalTime.of(8, 0).plusMinutes(opt.startSlot() * 30L);
            LocalTime end = start.plusMinutes(4 * 30L); // 2 hours assumed

            // Assign eligible + available students
            List<Student> assignedStudents = studentAssignments.getOrDefault(meta, List.of());

            assignStudentsToSession(meta, day, start, end, lecturer, opt.venue(), assignedStudents);
        }

        log.info("Backtracking scheduling completed. Total sessions created: {}", sessionToModuleIdMap.size());
    }


    private void persistAndGroupSessions(Map<Session, String> sessionToModuleIdMap) {
        Map<Session, Venue> newSessionVenueMap = new HashMap<>();
        List<PlanContentId> persistedPlanContentIds = new ArrayList<>();

        Optional<Satisfaction> optionalSatisfaction = satisfactionService.findLatestSatisfaction();

        for (Map.Entry<Session, String> entry : sessionToModuleIdMap.entrySet()) {
            Session originalSession = entry.getKey();
            String moduleId = entry.getValue();

            // Persist session
            Session savedSession = sessionService.saveSession(originalSession);
            Venue venue = sessionVenueMap.get(originalSession);
            if (venue != null) {
                VenueAssignment assignment = new VenueAssignment();

                VenueAssignmentId id = new VenueAssignmentId();
                id.setVenueId(venue.getId());
                id.setSessionId(savedSession.getId());

                assignment.setVenueAssignmentId(id);
                assignment.setVenue(venue);
                assignment.setSession(savedSession);

                venueAssignmentService.saveAssignment(assignment);

                newSessionVenueMap.put(savedSession, venue);
            }

            // Fetch module by ID
            Optional<Module> optionalModule = moduleService.getModuleById(moduleId);
            if (optionalModule.isEmpty()) {
                log.warn("Module {} not found in DB, skipping PlanContent saving.", moduleId);
                continue;
            }
            Module module = optionalModule.get();

            // Save PlanContent relation
            PlanContentId planContentId = new PlanContentId();
            planContentId.setModuleId(module.getId());
            planContentId.setSessionId(savedSession.getId());

            PlanContent planContent = new PlanContent();
            planContent.setPlanContentId(planContentId);
            planContent.setModule(module);
            planContent.setSession(savedSession);
            planContentService.savePlanContent(planContent);

            persistedPlanContentIds.add(planContentId);

            // Group by semester and module
            List<Integer> allSemesters = sessionGroupPreprocessorService.getSemestersForModule(moduleId);
            if (allSemesters != null) {
                for (Integer semester : allSemesters) {
                    sessionBySemesterAndModule
                        .computeIfAbsent(semester, k -> new HashMap<>())
                        .computeIfAbsent(module.getId(), k -> new ArrayList<>())
                        .add(savedSession);
                }
            } else {
                
                // Fallback to student's semester if module-semester mapping is missing
                Integer fallbackSemester = originalSession.getStudent() != null
                        ? studentSemesterMap.get(originalSession.getStudent().getId())
                        : null;

                if (fallbackSemester != null) {
                    sessionBySemesterAndModule
                        .computeIfAbsent(fallbackSemester, k -> new HashMap<>())
                        .computeIfAbsent(module.getId(), k -> new ArrayList<>())
                        .add(savedSession);
                }
            }
        }

        for (PlanContentId planContentId : persistedPlanContentIds) {
            PlanContent planContent = planContentService.getPlanContentById(planContentId)
                    .orElseThrow(() -> new IllegalStateException("PlanContent not found after save"));

            if (optionalSatisfaction.isPresent()) {
                Plan plan = new Plan(
                    new PlanId(planContentId, optionalSatisfaction.get().getId()),
                    planContent,
                    optionalSatisfaction.get()
                );
                planService.savePlan(plan);
            } else {
                log.warn("Skipping Plan save: no Satisfaction record found (likely first-time scheduling)");
            }
        }
        // Update the sessionVenueMap to use persisted session references
        sessionVenueMap.clear();
        sessionVenueMap.putAll(newSessionVenueMap);
    }

    @Transactional(readOnly = true)
    public List<File> exportPersistedTimetable(String programme, String intake, int year, double finalScore) {
        return timetableExcelExporter.exportWithFitnessAnnotation(sessionBySemesterAndModule, finalScore, programme, intake, year);
    }

    private void assignStudentsToSession(SessionGroupMetaData meta, String day, LocalTime start, LocalTime end, 
                                     Lecturer lecturer, Venue venue, List<Student> assignedStudents) {
        for (Student student : assignedStudents) {
            Session session = new Session();
            session.setType(meta.getType());
            session.setTypeGroup(meta.getTypeGroup());
            session.setLecturer(lecturer);
            session.setDay(day);
            session.setStartTime(start);
            session.setEndTime(end);
            session.setStudent(student);

            sessionToModuleIdMap.put(session, meta.getModuleId());
            sessionVenueMap.put(session, venue);

            String key = meta.getModuleId() + "-" + meta.getType().toUpperCase();
            studentAssignedTypes.computeIfAbsent(student.getId(), k -> new HashSet<>()).add(key);

            sessionBySemesterAndModule
                .computeIfAbsent(meta.getSemester(), k -> new HashMap<>())
                .computeIfAbsent(meta.getModuleId(), k -> new ArrayList<>())
                .add(session);
        }
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
                        String group = groupEntry.getKey().split("-")[2];
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

    private String getDayName(int index) {
        return switch (index) {
            case 0 -> "Monday";
            case 1 -> "Tuesday";
            case 2 -> "Wednesday";
            case 3 -> "Thursday";
            case 4 -> "Friday";
            default -> throw new IllegalArgumentException("Invalid day index: " + index);
        };
    }


    private int getDurationHours(String type) {
        return switch (type.toUpperCase()) {
            case "LECTURE" -> 2;
            case "TUTORIAL" -> 2;
            case "WORKSHOP" -> 2;
            case "PRACTICAL" -> 2;
            default -> 1;
        };
    }

    private int getDayIndex(String day) {
        return switch (day.toLowerCase()) {
            case "monday" -> 0;
            case "tuesday" -> 1;
            case "wednesday" -> 2;
            case "thursday" -> 3;
            case "friday" -> 4;
            default -> throw new IllegalArgumentException("Unknown day: " + day);
        };
    }

    private int getStartSlot(LocalTime startTime) {
        return (int) Duration.between(LocalTime.of(8, 0), startTime).toMinutes() / 30;
    }

    private void resetState() {
        lecturerMatrix.reset();
        venueMatrix.reset();
        studentMatrix.reset();

        studentAssignedTypes.clear();
        sessionToModuleIdMap.clear();
        sessionVenueMap.clear();
        lectureAssignmentsByModule.clear();
        lastAssignedVenuePerDay.clear();

        sessionGroupPreprocessorService.reset();
    }
}



