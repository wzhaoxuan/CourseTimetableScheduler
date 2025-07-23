package com.sunway.course.timetable.akka.actor;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sunway.course.timetable.engine.DomainPruner;
import com.sunway.course.timetable.engine.DomainPruner.AssignmentOption;
import com.sunway.course.timetable.engine.DomainRejectionReason;
import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.assignment.SessionGroupMetaData;
import com.sunway.course.timetable.service.LecturerServiceImpl;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
import com.sunway.course.timetable.singleton.StudentAvailabilityMatrix;
import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;
import com.sunway.course.timetable.util.LecturerDayAvailabilityUtil;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class SessionAssignmentActor extends AbstractBehavior<SessionAssignmentActor.SessionAssignmentCommand> {

    public interface SessionAssignmentCommand {}

    public static final class AssignSession implements SessionAssignmentCommand {
        public final int durationHours;
        public final int minCapacity;
        public final String lecturerName;
        public final Module module;
        public final List<Student> eligibleStudents;
        public final String sessionType;
        public final int groupIndex;
        public final int groupCount;
        public final ActorRef<SessionAssignmentResult> replyTo;
        public final ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> coordinator;
        public final List<String> preferredVenues;
        public final LecturerAvailabilityMatrix lecturerMatrix;
        public final VenueAvailabilityMatrix venueMatrix;
        public final StudentAvailabilityMatrix studentMatrix;
        public final List<Venue> allVenues;
        public final LecturerDayAvailabilityUtil lecturerDayAvailabilityUtil;
        public final LecturerServiceImpl lecturerService;
        public final Map<String, SessionAssigned> lectureAssignmentsByModule; // ADDED

        public AssignSession(int durationHours, int minCapacity, String lecturerName,
                              Module module, List<Student> eligibleStudents,
                              String sessionType, int groupIndex, int groupCount,
                              ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> coordinator,
                              ActorRef<SessionAssignmentResult> replyTo,
                              List<String> preferredVenues,
                              LecturerAvailabilityMatrix lecturerMatrix,
                              VenueAvailabilityMatrix venueMatrix,
                              StudentAvailabilityMatrix studentMatrix,
                              List<Venue> allVenues,
                              LecturerServiceImpl lecturerService,
                              LecturerDayAvailabilityUtil lecturerDayAvailabilityUtil,
                              Map<String, SessionAssigned> lectureAssignmentsByModule // ADDED
                              ) {
            this.durationHours = durationHours;
            this.minCapacity = minCapacity;
            this.lecturerName = lecturerName;
            this.module = module;
            this.eligibleStudents = eligibleStudents;
            this.sessionType = sessionType;
            this.groupIndex = groupIndex;
            this.groupCount = groupCount;
            this.coordinator = coordinator;
            this.replyTo = replyTo;
            this.preferredVenues = preferredVenues;
            this.lecturerMatrix = lecturerMatrix;
            this.venueMatrix = venueMatrix;
            this.studentMatrix = studentMatrix;
            this.allVenues = allVenues;
            this.lecturerService = lecturerService;
            this.lecturerDayAvailabilityUtil = lecturerDayAvailabilityUtil;
            this.lectureAssignmentsByModule = lectureAssignmentsByModule; // ADDED
        }
    }

    public interface SessionAssignmentResult extends SessionAssignmentCommand {}

    public static final class SessionAssigned implements SessionAssignmentResult {
        public final Venue venue;
        public final int dayIndex;
        public final int startIndex;
        public final int durationSlots;
        public final List<Student> assignedStudents;

        public SessionAssigned(Venue venue, int dayIndex, int startIndex, int durationSlots,
                               List<Student> assignedStudents) {
            this.venue = venue;
            this.dayIndex = dayIndex;
            this.startIndex = startIndex;
            this.durationSlots = durationSlots;
            this.assignedStudents = assignedStudents;
        }

        public List<Student> getAssignedStudents() {
            return assignedStudents;
        }
    }

    public static final class SessionAssignmentFailed implements SessionAssignmentResult {
        public final String reason;

        public SessionAssignmentFailed(String reason) { this.reason = reason; }
    }

    private final ActorContext<SessionAssignmentCommand> context;
    private ActorRef<SessionAssignmentResult> originalRequester;
    private static final int SLOTS_PER_CLASS = 4;

    public static Behavior<SessionAssignmentCommand> create() {
        return Behaviors.setup(SessionAssignmentActor::new);
    }

    private SessionAssignmentActor(ActorContext<SessionAssignmentCommand> context) {
        super(context);
        this.context = context;
    }

    @Override
    public Receive<SessionAssignmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(AssignSession.class, this::onAssignSession)
                .onMessage(SessionAssigned.class, this::onSessionAssigned)
                .onMessage(SessionAssignmentFailed.class, this::onSessionAssignmentFailed)
                .build();
    }

    private Behavior<SessionAssignmentCommand> onAssignSession(AssignSession msg) {
        this.originalRequester = msg.replyTo;

        SessionGroupMetaData meta = new SessionGroupMetaData();
        meta.setModuleId(msg.module.getId());
        meta.setType(msg.sessionType);
        meta.setTypeGroup("-G" + (msg.groupIndex + 1));
        meta.setLecturerName(msg.lecturerName);
        meta.setGroupIndex(msg.groupIndex);
        meta.setGroupCount(msg.groupCount);
        meta.setTotalStudents(msg.eligibleStudents.size());

        List<DomainRejectionReason> rejectionLogs = new ArrayList<>();

        // 1) Hard prune
        List<AssignmentOption> prunedDomain = DomainPruner.pruneDomain(
            msg.lecturerMatrix, msg.venueMatrix, msg.studentMatrix,
            msg.allVenues, meta, msg.eligibleStudents,
            msg.lecturerService, msg.lecturerDayAvailabilityUtil, rejectionLogs);

        if (prunedDomain.isEmpty()) {
            context.getLog().warn("Session assignment failed: No valid domain options");
            rejectionLogs.forEach(r -> context.getLog().warn(r.toString()));
            msg.replyTo.tell(new SessionAssignmentFailed("No valid domain options"));
            return this;
        }

         // 2) Soft-penalty sort
        prunedDomain.sort(Comparator
            .comparingInt((AssignmentOption opt) -> softPenalty(opt, msg))
            .thenComparingInt(AssignmentOption::startSlot)
        );

        // 3) Avoid 4 consecutive classes when possible
        List<AssignmentOption> good = prunedDomain.stream()
            .filter(opt -> !causesTooManyConsecutiveClasses(
                msg.lecturerName, opt.day(), opt.startSlot(), msg.lecturerMatrix))
            .collect(Collectors.toList());

        List<AssignmentOption> finalDomain = good.isEmpty() ? prunedDomain : good;

        // 4) Delegate
        msg.coordinator.tell(new VenueCoordinatorActor.RequestVenueAssignment(
            msg.durationHours, msg.minCapacity, msg.lecturerName,
            msg.module, msg.eligibleStudents, msg.sessionType,
            msg.groupIndex, msg.groupCount, context.getSelf(),
            msg.preferredVenues, finalDomain, rejectionLogs));
        return this;
    }

    private Behavior<SessionAssignmentCommand> onSessionAssigned(SessionAssigned msg) {
        if (originalRequester != null) {
            originalRequester.tell(msg);
        }
        originalRequester = null;
        return this;
    }

    private Behavior<SessionAssignmentCommand> onSessionAssignmentFailed(SessionAssignmentFailed msg) {
        context.getLog().warn("Session assignment failed: {}", msg.reason);

        if (originalRequester != null) {
            originalRequester.tell(msg);
        }
        originalRequester = null;
        return this;
    }

    /**
     * Calculates the soft penalty for an assignment option based on various factors:
     * - Solo sessions for students
     * - Time of day
     * - Long gaps between sessions
     * - Spread of students across days
     * - New day for lecturer
     * - Lecture after practical penalty
     * - Too many consecutive classes for lecturer
     *
     * @param opt The assignment option being evaluated
     * @param msg The AssignSession message containing context
     * @return The calculated soft penalty score
     */
    private int softPenalty(AssignmentOption opt, AssignSession msg) {
        long solo = msg.eligibleStudents.stream()
            .filter(s -> causesOnlyOneSessionDay(
                s.getId(), opt.day(), opt.startSlot(), msg.studentMatrix))
            .count();
        double soloProp = (double) solo / msg.eligibleStudents.size();
        int timePen = getTimeOfDayPenalty(opt.startSlot());
        long gapPen = msg.eligibleStudents.stream()
            .filter(s -> causesLongGap(
                s.getId(), opt.day(), opt.startSlot(), msg.studentMatrix))
            .count();
        long spreadStu = msg.eligibleStudents.stream()
            .filter(s -> isNewDayForStudent(
                s.getId(), opt.day(), msg.studentMatrix))
            .count();
        boolean lecNewDay = isNewDayForLecturer(
            msg.lecturerName, opt.day(), msg.lecturerMatrix);
        int spreadLec = lecNewDay ? 1 : 0;
        int seqPen = isNotLectureSession(msg.sessionType)
            ? calculateLectureAfterPenalty(msg, opt) : 0;
        boolean tooMany = causesTooManyConsecutiveClasses(
            msg.lecturerName, opt.day(), opt.startSlot(), msg.lecturerMatrix);
        int overCon = tooMany ? 1 : 0;

        long stuTooManyCount = msg.eligibleStudents.stream()
            .filter(s -> causesTooManyConsecutiveClassesForStudent(
                s.getId(), opt.day(), opt.startSlot(), msg.studentMatrix))
            .count();
        int stuOverConPen = (int) stuTooManyCount * 500;

        return timePen * 300 + (int) gapPen * 100 + (int) soloProp * 120
            + (int) spreadStu * 90 + spreadLec * 90 + seqPen * 80 + overCon * 500 + stuOverConPen;
    }


    /**
     * Checks if assigning a session at the given start slot causes a long gap
     * (more than 120 minutes) between this session and any existing sessions
     * for the student on the same day.
     *
     * @param studentId The ID of the student
     * @param day The day index (0-4, where 0 is Monday)
     * @param startSlot The starting slot index (0-39, where each slot is 30 minutes)
     * @param studentMatrix The matrix containing student availability
     * @return true if it causes a long gap, false otherwise
     */
    private boolean causesLongGap(long studentId, int day, int startSlot, StudentAvailabilityMatrix studentMatrix) {
        List<LocalTime> assignedSlots = studentMatrix.getAssignedDays(studentId, day);
        LocalTime thisStart = LocalTime.of(8, 0).plusMinutes(startSlot * 30L);
        LocalTime thisEnd = thisStart.plusMinutes(120);

        for (LocalTime time : assignedSlots) {
            long gapAfter = Math.abs(Duration.between(thisEnd, time).toMinutes());
            long gapBefore = Math.abs(Duration.between(time, thisStart).toMinutes());

            if (gapAfter > 120 || gapBefore > 120) return true;
        }
        return false;
    }


    /**
     * Returns a penalty based on the time of day:
     * - 0 for morning/afternoon (before 6:00 PM)
     * - 1 for late (after 6:00 PM)
     *
     * @param startSlot The starting slot index (0-39, where each slot is 30 minutes)
     * @return The penalty score
     */
    private int getTimeOfDayPenalty(int startSlot) {
        LocalTime start = LocalTime.of(8, 0).plusMinutes(startSlot * 30L);

        if (start.isBefore(LocalTime.of(18, 0))) return 0;   // Morning or Afternoon
        else return 1;                                            // Late (after 6:00 PM)
    }

    /**
     * Checks if assigning a session on the given day causes only one session
     * to be assigned for the student on that day.
     *
     * @param studentId The ID of the student
     * @param day The day index (0-4, where 0 is Monday)
     * @param startSlot The starting slot index (0-39, where each slot is 30 minutes)
     * @param matrix The student availability matrix
     * @return true if it causes only one session, false otherwise
     */
    private boolean causesOnlyOneSessionDay(Long studentId, int day, int startSlot, StudentAvailabilityMatrix matrix) {
        List<LocalTime> occupied = matrix.getAssignedDays(studentId, day);
        return occupied.size() == 0; // Only one slot used on this day
    }

    /**
     * Checks if the session type is not a lecture.
     * This is used to determine if the session is a practical, tutorial, or workshop.
     *
     * @param type The type of the session
     * @return true if it is not a lecture session, false otherwise
     */
    private boolean isNotLectureSession(String type) {
        return type.equalsIgnoreCase("Practical") 
            || type.equalsIgnoreCase("Tutorial") 
            || type.equalsIgnoreCase("Workshop");
    }

    /**
     * Calculates the penalty for scheduling a practical session after a lecture.
     * If the practical is scheduled before the lecture, it incurs a penalty.
     * If it is scheduled after the lecture, there is no penalty.
     *
     * @param msg The AssignSession message containing context
     * @param opt The assignment option being evaluated
     * @return 1 if there is a penalty, 0 if it is OK
     */
    private int calculateLectureAfterPenalty(AssignSession msg, AssignmentOption opt) {
        SessionAssigned lecture = msg.lectureAssignmentsByModule.get(msg.module.getId());
        if (lecture == null) return 0; // no penalty if no lecture exists yet

        if (opt.day() < lecture.dayIndex) return 1;  // schedule before lecture → penalty
        if (opt.day() == lecture.dayIndex && opt.startSlot() < (lecture.startIndex + lecture.durationSlots)) return 1;

        return 0; 
    }

    /**
     * Checks if the lecturer is assigned on a new day.
     * This means the lecturer has no sessions assigned on that day.
     *
     * @param lecturerName The name of the lecturer
     * @param day The day index (0-4, where 0 is Monday)
     * @param lecturerMatrix The matrix containing lecturer availability
     * @return true if it is a new day for the lecturer, false otherwise
     */
    private boolean isNewDayForLecturer(String lecturerName, int day, LecturerAvailabilityMatrix lecturerMatrix) {
        return lecturerMatrix.getAssignedDays(lecturerName).contains(day) == false;
    }

    /**
     * Checks if the student has no sessions assigned on the given day.
     * This means the student is available for a new session on that day.
     *
     * @param studentId The ID of the student
     * @param day The day index (0-4, where 0 is Monday)
     * @param studentMatrix The matrix containing student availability
     * @return true if it is a new day for the student, false otherwise
     */
    private boolean isNewDayForStudent(Long studentId, int day, StudentAvailabilityMatrix studentMatrix) {
        List<LocalTime> occupied = studentMatrix.getAssignedDays(studentId, day);
        return occupied.isEmpty();  // If student has no sessions that day
    }

    /**
     * Checks if assigning a session would cause the lecturer to have too many consecutive classes.
     * This is used to avoid scheduling a session that would leave the lecturer with
     * 4 or more consecutive classes on that day.
     *
     * @param lecturerName The name of the lecturer
     * @param day The day index (0-4, where 0 is Monday)
     * @param startSlot The starting slot index (0-39, where each slot is 30 minutes)
     * @param matrix The lecturer availability matrix
     * @return true if it causes too many consecutive classes, false otherwise
     */
    private boolean causesTooManyConsecutiveClasses(
            String lecturerName, int day, int startSlot,
            LecturerAvailabilityMatrix matrix) {
        boolean[] occ = matrix.getDailyAvailabilityArray(lecturerName, day);
        boolean[] newOcc = Arrays.copyOf(occ, occ.length);
        int end = Math.min(startSlot + SLOTS_PER_CLASS, newOcc.length);
        for (int i = startSlot; i < end; i++) newOcc[i] = true;
        int maxRun = 0, run = 0;
        for (boolean o : newOcc) {
            if (o) { run++; maxRun = Math.max(maxRun, run); }
            else run = 0;
        }
        int maxSlots = SLOTS_PER_CLASS * 4; // avoid 4 classes back-to-back
        return maxRun >= maxSlots;
    }

    /**
     * Checks if the student has too many consecutive classes on the same day.
     * This is used to avoid scheduling a session that would leave the student with
     * 4 or more consecutive classes on that day.
     *
     * @param studentId The ID of the student
     * @param day The day index (0-4, where 0 is Monday)
     * @param startSlot The starting slot index (0-39, where each slot is 30 minutes)
     * @param matrix The student availability matrix
     * @return true if it causes too many consecutive classes, false otherwise
     */
    private boolean causesTooManyConsecutiveClassesForStudent(
            long studentId, int day, int startSlot, 
            StudentAvailabilityMatrix matrix) {
        boolean[] occ = matrix.getDailyAvailabilityArray(studentId, day);
        boolean[] newOcc = Arrays.copyOf(occ, occ.length);
        int end = Math.min(startSlot + SLOTS_PER_CLASS, newOcc.length);
        for (int i = startSlot; i < end; i++) newOcc[i] = true;
        int maxRun = 0, run = 0;
        for (boolean o : newOcc) {
            if (o) { run++; maxRun = Math.max(maxRun, run); }
            else run = 0;
        }
        return maxRun >= SLOTS_PER_CLASS * 4;
    }
}