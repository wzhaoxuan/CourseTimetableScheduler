package com.sunway.course.timetable.akka.actor;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.sunway.course.timetable.engine.DomainPruner;
import com.sunway.course.timetable.engine.DomainPruner.AssignmentOption;
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

        List<AssignmentOption> prunedDomain = DomainPruner.pruneDomain(
            msg.lecturerMatrix,
            msg.venueMatrix,
            msg.studentMatrix,
            msg.allVenues,
            meta,
            msg.eligibleStudents,
            msg.lecturerService,
            msg.lecturerDayAvailabilityUtil
        );

        prunedDomain.sort(Comparator
            .comparingInt((AssignmentOption opt) -> {
                // One-session-per-day penalty for students
                long soloDayPenalty = msg.eligibleStudents.stream()
                    .filter(s -> causesOnlyOneSessionDay(s.getId(), opt.day(), opt.startSlot(), msg.studentMatrix))
                    .count();
                int timePenalty = getTimeOfDayPenalty(opt.startSlot()); // Lower = better
                long gapPenalty = msg.eligibleStudents.stream()
                    .filter(s -> causesLongGap(s.getId(), opt.day(), opt.startSlot(), msg.studentMatrix))
                    .count();
                
                    long spreadPenaltyStudents = msg.eligibleStudents.stream()
                    .filter(s -> isNewDayForStudent(s.getId(), opt.day(), msg.studentMatrix))
                    .count();

                boolean lecturerNewDay = isNewDayForLecturer(msg.lecturerName, opt.day(), msg.lecturerMatrix);
                int spreadPenaltyLecturer = lecturerNewDay ? 1 : 0;

                int sequencingPenalty = 0;
                if (isDependentSession(msg.sessionType)) {
                    sequencingPenalty = calculateLectureAfterPenalty(msg, opt);
                }
                
                return timePenalty * 1000 + (int) gapPenalty + (int) soloDayPenalty * 5 + sequencingPenalty * 500 +
                (int) spreadPenaltyStudents * 2 + spreadPenaltyLecturer * 3 ;  // Weighted
            })
            .thenComparingInt(AssignmentOption::startSlot)
        );


        msg.coordinator.tell(new VenueCoordinatorActor.RequestVenueAssignment(
            msg.durationHours, msg.minCapacity, msg.lecturerName, msg.module, msg.eligibleStudents,
            msg.sessionType, msg.groupIndex, msg.groupCount,
            context.getSelf(), msg.preferredVenues, prunedDomain
        ));

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


    private boolean causesLongGap(long studentId, int day, int startSlot, StudentAvailabilityMatrix studentMatrix) {
        List<LocalTime> assignedSlots = studentMatrix.getAssignedTimes(studentId, day);
        LocalTime thisStart = LocalTime.of(8, 0).plusMinutes(startSlot * 30L);
        LocalTime thisEnd = thisStart.plusMinutes(120);

        for (LocalTime time : assignedSlots) {
            long gapAfter = Math.abs(Duration.between(thisEnd, time).toMinutes());
            long gapBefore = Math.abs(Duration.between(time, thisStart).toMinutes());

            if (gapAfter > 120 || gapBefore > 120) return true;
        }
        return false;
    }


    private int getTimeOfDayPenalty(int startSlot) {
        LocalTime start = LocalTime.of(8, 0).plusMinutes(startSlot * 30L);

        if (start.isBefore(LocalTime.of(11, 0))) return 0;        // Morning
        else if (start.isBefore(LocalTime.of(16, 0))) return 1;   // Afternoon
        else return 2;                                            // Late (after 4:00 PM)
    }

    private boolean causesOnlyOneSessionDay(Long studentId, int day, int startSlot, StudentAvailabilityMatrix matrix) {
        List<LocalTime> occupied = matrix.getAssignedTimes(studentId, day);
        return occupied.size() == 0; // Only one slot used on this day
    }

    private boolean isDependentSession(String type) {
        return type.equalsIgnoreCase("Practical") 
            || type.equalsIgnoreCase("Tutorial") 
            || type.equalsIgnoreCase("Workshop");
    }

    private int calculateLectureAfterPenalty(AssignSession msg, AssignmentOption opt) {
        SessionAssigned lecture = msg.lectureAssignmentsByModule.get(msg.module.getId());
        if (lecture == null) return 0; // no penalty if no lecture exists yet

        if (opt.day() < lecture.dayIndex) return 1;  // schedule before lecture → penalty
        if (opt.day() == lecture.dayIndex && opt.startSlot() < (lecture.startIndex + lecture.durationSlots)) return 1;

        return 0; // OK, practical is after lecture
    }

    private boolean isNewDayForLecturer(String lecturerName, int day, LecturerAvailabilityMatrix lecturerMatrix) {
        return lecturerMatrix.getAssignedDays(lecturerName).contains(day) == false;
    }

    private boolean isNewDayForStudent(Long studentId, int day, StudentAvailabilityMatrix studentMatrix) {
        List<LocalTime> occupied = studentMatrix.getAssignedTimes(studentId, day);
        return occupied.isEmpty();  // If student has no sessions that day
    }


}