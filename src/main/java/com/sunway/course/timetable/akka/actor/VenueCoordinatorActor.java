package com.sunway.course.timetable.akka.actor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
import com.sunway.course.timetable.singleton.StudentAvailabilityMatrix;
import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class VenueCoordinatorActor extends AbstractBehavior<VenueCoordinatorActor.VenueCoordinatorCommand>
    implements VenueActor.VenueResponse {
    
    private static final Logger log = LoggerFactory.getLogger(VenueCoordinatorActor.class);
    
    // === Constants ===
    private static final int MAX_DAYS = 5;
    private static final int MAX_SLOTS_PER_DAY = 20;

    public interface VenueCoordinatorCommand {}

    public static final class InitializeVenues implements VenueCoordinatorCommand {
        public final List<Venue> venues;
        public final List<ActorRef<VenueActor.VenueCommand>> venueActors;

        public InitializeVenues(List<Venue> venues, List<ActorRef<VenueActor.VenueCommand>> venueActors) {
            this.venues = venues;
            this.venueActors = venueActors;
        }
    }

    public static final class RequestVenueAssignment implements VenueCoordinatorCommand {
        public final int durationHours;
        public final int minCapacity;
        public final String lecturerName;
        public final Module module;
        public final List<Student> eligibleStudents;
        public final String sessionType;
        public final int groupIndex;
        public final int groupCount;
        public final ActorRef<SessionAssignmentActor.SessionAssignmentCommand> replyTo;

        public RequestVenueAssignment(int durationHours, int minCapacity, String lecturerName,
                                        Module module, List<Student> eligibleStudents,
                                        String sessionType, 
                                        int groupIndex,
                                        int groupCount,
                                  ActorRef<SessionAssignmentActor.SessionAssignmentCommand> replyTo) {
            this.durationHours = durationHours;
            this.minCapacity = minCapacity;
            this.lecturerName = lecturerName;
            this.module = module;
            this.eligibleStudents = eligibleStudents;
            this.sessionType = sessionType;
            this.groupIndex = groupIndex;
            this.groupCount = groupCount;
            this.replyTo = replyTo;
        }
    }

    public static final class VenueAssignmentAttempt implements VenueCoordinatorCommand {
        public final Venue venue;
        public final int dayIndex;
        public final int startIndex;
        public final int durationSlots;
        public final ActorRef<VenueActor.VenueResponse> replyTo;

        public VenueAssignmentAttempt(Venue venue, int dayIndex, int startIndex, int durationSlots,
                                      ActorRef<VenueActor.VenueResponse> replyTo) {
            this.venue = venue;
            this.dayIndex = dayIndex;
            this.startIndex = startIndex;
            this.durationSlots = durationSlots;
            this.replyTo = replyTo;
        }
    }

    public static final class VenueAcceptedMsg implements VenueCoordinatorCommand {
        public final VenueActor.VenueAccepted accepted;

        public VenueAcceptedMsg(VenueActor.VenueAccepted accepted) {
            this.accepted = accepted;
        }
    }

    public static final class VenueRejectedMsg implements VenueCoordinatorCommand {
        public final VenueActor.VenueRejected rejected;

        public VenueRejectedMsg(VenueActor.VenueRejected rejected) {
            this.rejected = rejected;
        }
    }

     // === Internal State ===
    private final List<ActorRef<VenueActor.VenueCommand>> venueActors;
    private final List<Venue> venues; // parallel list
    private final LecturerAvailabilityMatrix lecturerAvailability;
    private final VenueAvailabilityMatrix venueAvailability;
    private final StudentAvailabilityMatrix studentAvailability;
    private final ActorRef<VenueActor.VenueResponse> venueResponseAdapter;
    private AssignmentState currentAssignment;

    private record AssignmentState(RequestVenueAssignment request,
                                   int dayIndex,
                                   int startIndex,
                                   int durationSlots,
                                   int venueIndex) {}

    // === Factory Method ===
    public static Behavior<VenueCoordinatorCommand> create(List<Venue> venues,
                                                           List<ActorRef<VenueActor.VenueCommand>> venueActors,
                                                           LecturerAvailabilityMatrix lecturerAvailability,
                                                           VenueAvailabilityMatrix venueAvailability,
                                                           StudentAvailabilityMatrix studentAvailability) {
        return Behaviors.setup(context -> new VenueCoordinatorActor(context,
            venues,
            venueActors,
            lecturerAvailability,
            venueAvailability,
            studentAvailability));
    }


    private VenueCoordinatorActor(ActorContext<VenueCoordinatorCommand> context,
                                  List<Venue> venues,
                                  List<ActorRef<VenueActor.VenueCommand>> venueActors,
                                  LecturerAvailabilityMatrix lecturerAvailability,
                                  VenueAvailabilityMatrix venueAvailability,
                                  StudentAvailabilityMatrix studentAvailability) {
        super(context);
        this.venues = venues;
        this.venueActors = venueActors;
        this.lecturerAvailability = lecturerAvailability;
        this.venueAvailability = venueAvailability;
        this.studentAvailability = studentAvailability;

        // Message adapter: converts VenueResponse to VenueCoordinatorCommand
        this.venueResponseAdapter = context.messageAdapter(
            VenueActor.VenueResponse.class,
            response -> {
                if (response instanceof VenueActor.VenueAccepted accepted) {
                    return new VenueAcceptedMsg(accepted);
                } else if (response instanceof VenueActor.VenueRejected rejected) {
                    return new VenueRejectedMsg(rejected);
                } else {
                    getContext().getLog().warn("Unhandled VenueResponse: {}", response);
                    return null;
                }
            });
    }

    // === Message Handlers ===
     @Override
    public Receive<VenueCoordinatorCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(RequestVenueAssignment.class, this::onRequestVenueAssignment)
                .onMessage(VenueAcceptedMsg.class, this::onVenueAccepted)
                .onMessage(VenueRejectedMsg.class, this::onVenueRejected)
                .build();
    }


    private Behavior<VenueCoordinatorCommand> onRequestVenueAssignment(RequestVenueAssignment msg) {
        // getContext().getLog().info("Venue Actor Size:{}, Venue Size: {}",
        //         venueActors.size(), venues.size());
        
        if (venueActors.size() != venues.size()) {
            log.warn("Venue list or venueActors list empty or mismatched! Cannot perform venue assignment.");
        }

        
        if (venues.isEmpty()) {
            log.warn("No venues available for assignment.");
            msg.replyTo.tell(new SessionAssignmentActor.SessionAssignmentFailed("No venues available"));
            currentAssignment = null;
            return this;
        }
        
        int durationSlots = msg.durationHours * 2; // assuming 30-min slots, 2 slots per hour
        if (durationSlots > MAX_SLOTS_PER_DAY) {
            msg.replyTo.tell(new SessionAssignmentActor.SessionAssignmentFailed("Session too long for one day"));
            return this;
        }
        currentAssignment = new AssignmentState(msg, 0, 0, durationSlots, 0);
        tryNextAssignment();
        return this;
    }

    /**
     *  Tries to assign the next available slot for the current assignment.
     *  This method iterates through the days and venues,
     *   checking availability of the lecturer, venue, and students.
     * 
     */
    private void tryNextAssignment() {
        while (currentAssignment != null) {
            int day = currentAssignment.dayIndex;
            int start = currentAssignment.startIndex;
            int durationSlots = currentAssignment.durationSlots;
            int venueIdx = currentAssignment.venueIndex;
            var req = currentAssignment.request;

            // log.info("[TRY] Module={} Type={} Group=G{} Day={} Slot={}-{} VenueIndex={}",
            //     req.module.getId(), req.sessionType, (req.groupIndex + 1), day, start, start + durationSlots - 1, venueIdx);

            if (venueIdx >= venues.size()) {
                // log.info("[NEXT SLOT] All venues tried at slot {}, moving to next slot", start);
                start++;
                venueIdx = 0;

                if (start + durationSlots > MAX_SLOTS_PER_DAY) {
                    // log.info("[SKIP] Start {} + duration {} exceeds max slots", start, durationSlots);
                    day++;
                    start = 0;

                    if (day >= MAX_DAYS) {
                        log.warn("Day {} exceeds maximum {}", day, MAX_DAYS);
                        req.replyTo.tell(new SessionAssignmentActor.SessionAssignmentFailed("No valid schedule found for this session"));
                        currentAssignment = null;
                        return;
                    }
                }

                currentAssignment = new AssignmentState(req, day, start, durationSlots, venueIdx);
                continue;
            }

            Venue venue = venues.get(venueIdx);
            // log.info("[CHECK] Venue={} Capacity={} MinRequired={}", venue.getName(), venue.getCapacity(), req.minCapacity);

            boolean lecturerOk = lecturerAvailability.isAvailable(req.lecturerName, day, start, start + durationSlots);
            boolean venueOk = venueAvailability.isAvailable(venue, start, start + durationSlots, day);

            // log.info("[AVAIL] Lecturer={} Available={} VenueAvailable={}", req.lecturerName, lecturerOk, venueOk);

            final int filterDay = day;
            final int filterStart = start;
            final int filterEnd = start + durationSlots;

            List<Long> availableStudents = req.eligibleStudents.stream()
                .map(Student::getId)
                .filter(id -> studentAvailability.isAvailable(id, filterDay, filterStart, filterEnd))
                .toList();

            // log.info("[STUDENT CHECK] Eligible={} Available={} Type={}", req.eligibleStudents.size(), availableStudents.size(), req.sessionType);

            if (!lecturerOk || !venueOk || venue.getCapacity() < req.minCapacity || availableStudents.isEmpty()) {
                // log.info("[SKIP] Constraints not met. Trying next venue.");
                currentAssignment = new AssignmentState(req, day, start, durationSlots, venueIdx + 1);
                continue;
            }

            // Proceed with assignment
            venueActors.get(venueIdx).tell(new VenueActor.CheckAndAssignSlot(
                day, start, start + durationSlots, req.lecturerName, venueResponseAdapter
            ));

            return; // wait for response
        }
    }        


        private Behavior<VenueCoordinatorCommand> onVenueAccepted(VenueAcceptedMsg msg) {
            if (currentAssignment == null) return this;

            var accepted = msg.accepted;
            var req = currentAssignment.request;

            // Update availability matrices
            lecturerAvailability.assign(req.lecturerName,
                accepted.dayIndex, accepted.startIndex, accepted.startIndex + accepted.durationSlots);
            venueAvailability.assign(
                accepted.venue,
                accepted.startIndex, accepted.startIndex + accepted.durationSlots, accepted.dayIndex);

            List<Student> allEligible = req.eligibleStudents;
            List<Student> assignedStudents;

            if (req.sessionType.equalsIgnoreCase("lecture")) {
                // Assign all students for lecture
                assignedStudents = allEligible.stream()
                    .filter(s -> studentAvailability.isAvailable(
                        s.getId(),
                        accepted.dayIndex,
                        accepted.startIndex,
                        accepted.startIndex + accepted.durationSlots
                    ))
                    .toList();

            } else {
                // Assign group of students for practical/tutorial/workshop
                int groupSize = (int) Math.ceil((double) allEligible.size() / req.groupCount);
                int startIdx = req.groupIndex * groupSize;
                int endIdx = Math.min(startIdx + groupSize, allEligible.size());

                assignedStudents = allEligible.subList(startIdx, endIdx).stream()
                    .filter(s -> studentAvailability.isAvailable(
                        s.getId(),
                        accepted.dayIndex,
                        accepted.startIndex,
                        accepted.startIndex + accepted.durationSlots
                    ))
                    .toList();
            }

            final int filterDay = accepted.dayIndex;
            final int filterStart = accepted.startIndex;
            final int filterEnd = accepted.startIndex + accepted.durationSlots;

            // Filter by student availability
            assignedStudents = assignedStudents.stream()
                .filter(s -> studentAvailability.isAvailable(
                    s.getId(),
                    filterDay,
                    filterStart,
                    filterEnd
                ))
                .toList();

            if (assignedStudents.isEmpty()) {
                req.replyTo.tell(new SessionAssignmentActor.SessionAssignmentFailed("No available students for this slot"));
                currentAssignment = null;
                return this;
            } 

            // Determine how many students we need
            int expectedMinimum = switch (req.sessionType.toLowerCase()) {
                case "lecture" -> (int) (req.eligibleStudents.size() * 1); // 80% of all
                default -> {
                    int groupSize = (int) Math.ceil((double) req.eligibleStudents.size() / req.groupCount);
                    yield (int) (groupSize * 1); // 70% of this group
                }
            };

            if (assignedStudents.size() < expectedMinimum) {
                int nextStart = currentAssignment.startIndex + 1;
                int nextDay = currentAssignment.dayIndex;

                if (nextStart + currentAssignment.durationSlots > MAX_SLOTS_PER_DAY) {
                    nextStart = 0;
                    nextDay++;
                }

                if (nextDay >= MAX_DAYS) {
                    req.replyTo.tell(new SessionAssignmentActor.SessionAssignmentFailed("No days available"));
                    currentAssignment = null;
                    return this;
                }

                // Try next day/slot, restart the venue index
                currentAssignment = new AssignmentState(req, nextDay, nextStart, currentAssignment.durationSlots, 0);
                tryNextAssignment();
                return this;
        }

            // Mark students as unavailable for the assigned session time
            for (Student studentId : assignedStudents) {
                studentAvailability.markUnavailable(studentId.getId(), filterDay, filterStart, filterEnd);
            }

            // Reply to session actor
            req.replyTo.tell(new SessionAssignmentActor.SessionAssigned(
                accepted.venue,
                accepted.dayIndex,
                accepted.startIndex,
                accepted.durationSlots,
                assignedStudents
            ));
            

        log.info("Assigned {} {} on Day {}, Time {}. Venue {}, Students: {}",
                req.module.getId(), req.sessionType,
                accepted.dayIndex, accepted.startIndex,
                accepted.venue.getName(),
                assignedStudents.size());

            currentAssignment = null;
            return this;
    }



    private Behavior<VenueCoordinatorCommand> onVenueRejected(VenueRejectedMsg msg) {
        if (currentAssignment == null) return this;

        // Try next venue for same time
        currentAssignment = new AssignmentState(
            currentAssignment.request,
            currentAssignment.dayIndex,
            currentAssignment.startIndex,
            currentAssignment.durationSlots,
            currentAssignment.venueIndex + 1
        );

        tryNextAssignment();
        return this;
    }
}
