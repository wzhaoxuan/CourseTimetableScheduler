package com.sunway.course.timetable.akka.actor;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class VenueActor extends AbstractBehavior<VenueActor.VenueCommand> {

    public interface VenueCommand {}

    public static final class CheckAndAssignSlot implements VenueCommand {
        public final int dayIndex;
        public final int startIndex;
        public final int endIndex;
        public final String lecturerId;
        public final ActorRef<VenueResponse> replyTo;

        public CheckAndAssignSlot(int dayIndex, int startIndex, int endIndex,
                                  String lecturerId, ActorRef<VenueResponse> replyTo) {
            this.dayIndex = dayIndex;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.lecturerId = lecturerId;
            this.replyTo = replyTo;
        }
    }

    public interface VenueResponse {}

    public static final class VenueAccepted implements VenueResponse {
        public final Venue venue;
        public final int dayIndex;
        public final int startIndex;
        public final int durationSlots;

        public VenueAccepted(Venue venue, int dayIndex, int startIndex, int durationSlots) {
            this.venue = venue;
            this.dayIndex = dayIndex;
            this.startIndex = startIndex;
            this.durationSlots = durationSlots;
        }
    }

    public static final class VenueRejected implements VenueResponse {
        public final Venue venue;
        public final String reason;

        public VenueRejected(Venue venue, String reason) {
            this.venue = venue;
            this.reason = reason;
        }
    }

    private final Venue venue;
    private final int DAYS = 5; // Monday to Friday
    private final int TIME_SLOTS_PER_DAY = 20; // 8am to 6pm 30-min slots
    private final LecturerAvailabilityMatrix lecturerAvailability;
    private final VenueAvailabilityMatrix venueAvailability;
    private final int venueIndex; 

    // Local availability cache per venue for quick checks
    private final boolean[][] availability;

    public static Behavior<VenueCommand> create(
            Venue venue,
            VenueAvailabilityMatrix venueAvailability,
            LecturerAvailabilityMatrix lecturerAvailability) {
            Integer venueIndex = venueAvailability.getIndexForVenue(venue);

        if (venueIndex == null) {
            throw new IllegalArgumentException("Venue not found in availability matrix: " + venue.getId());
        }
        return Behaviors.setup(ctx -> new VenueActor(ctx, venue, venueIndex, venueAvailability, lecturerAvailability));
    }

    private VenueActor(
            ActorContext<VenueCommand> context,
            Venue venue,
            int venueIndex,
            VenueAvailabilityMatrix venueAvailability,
            LecturerAvailabilityMatrix lecturerAvailability) {
        super(context);
        this.venue = venue;
        this.venueIndex = venueIndex;
        this.venueAvailability = venueAvailability;
        this.lecturerAvailability = lecturerAvailability;
        this.availability = new boolean[TIME_SLOTS_PER_DAY][DAYS];

        boolean[][][] globalAvailability = venueAvailability.getAvailability();

        int venueCount = globalAvailability.length;
        if (venueIndex < 0 || venueIndex >= venueCount) {
            throw new IllegalStateException("venueIndex out of range: " + venueIndex);
        }

        int slotCount = globalAvailability[venueIndex].length;
        int dayCount = globalAvailability[venueIndex][0].length; // assume all slots have same day length

        // System.out.println("globalAvailability[venueIndex] dimensions: slots=" + slotCount + ", days=" + dayCount);

        if (slotCount < TIME_SLOTS_PER_DAY) {
            throw new IllegalStateException("Time slots dimension smaller than expected: " + slotCount);
        }
        if (dayCount < DAYS) {
            throw new IllegalStateException("Day dimension smaller than expected: " + dayCount);
        }

        for (int day = 0; day < DAYS; day++) {
            for (int slot = 0; slot < TIME_SLOTS_PER_DAY; slot++) {
                availability[slot][day] = globalAvailability[venueIndex][slot][day];
            }
        }
    }

    @Override
    public Receive<VenueCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(CheckAndAssignSlot.class, this::onCheckAndAssignSlot)
                .build();
    }

    private Behavior<VenueCommand> onCheckAndAssignSlot(CheckAndAssignSlot msg) {

        // getContext().getLog().info("Checking venue '{}' day {} slots {}-{}", venue.getName(), msg.dayIndex, msg.startIndex, msg.endIndex);

        if (msg.dayIndex < 0 || msg.dayIndex >= DAYS || msg.startIndex < 0 || msg.endIndex > TIME_SLOTS_PER_DAY || msg.startIndex >= msg.endIndex) {
            msg.replyTo.tell(new VenueRejected(venue, "Invalid day or time slots"));
            return this;
        }

        if (!lecturerAvailability.isAvailable(msg.lecturerId, msg.dayIndex, msg.startIndex, msg.endIndex)) {
            getContext().getLog().warn("Lecturer {} unavailable at day {} slots {}-{}", 
                msg.lecturerId, msg.dayIndex, msg.startIndex, msg.endIndex);
            lecturerAvailability.printAvailability(msg.lecturerId);
            msg.replyTo.tell(new VenueRejected(venue, "Lecturer unavailable"));
            return this;
        }

        // Check local availability
        for (int slot = msg.startIndex; slot < msg.endIndex; slot++) {
            if (slot >= TIME_SLOTS_PER_DAY || msg.dayIndex >= DAYS) {
                msg.replyTo.tell(new VenueRejected(venue, "Invalid time slot/day"));
                return this;
            }
            if (availability[slot][msg.dayIndex]) {
                msg.replyTo.tell(new VenueRejected(venue, "Venue already booked for requested slots"));
                return this;
            }
        }

        // Check global availability matrix (should be consistent with local)
        boolean[][][] globalAvailability = venueAvailability.getAvailability();
        for (int slot = msg.startIndex; slot < msg.endIndex; slot++) {
            if (globalAvailability[venueIndex][slot][msg.dayIndex]) {
                msg.replyTo.tell(new VenueRejected(venue, "Venue globally unavailable for requested slots"));
                return this;
            }
        }

        // Mark booked locally and globally
        for (int slot = msg.startIndex; slot < msg.endIndex; slot++) {
            availability[slot][msg.dayIndex] = false; // mark as booked
        }


        // Update global singleton matrix
        venueAvailability.assign(venue, msg.startIndex, msg.endIndex, msg.dayIndex);

        // Update lecturer availability as well
        lecturerAvailability.assign(msg.lecturerId, msg.dayIndex, msg.startIndex, msg.endIndex);

        int durationSlots = msg.endIndex - msg.startIndex;
        msg.replyTo.tell(new VenueAccepted(venue, msg.dayIndex, msg.startIndex, durationSlots));
        return this;
    }
}

