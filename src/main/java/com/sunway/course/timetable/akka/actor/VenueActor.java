package com.sunway.course.timetable.akka.actor;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.Arrays;

import com.sunway.course.timetable.model.Venue;


public class VenueActor extends AbstractBehavior<VenueActor.VenueCommand> {

    public interface VenueCommand {}

    public static final class CheckAndAssignSlot implements VenueCommand {
        public final int dayIndex;
        public final int startIndex;
        public final int endIndex;
        public final ActorRef<VenueResponse> replyTo;

        public CheckAndAssignSlot(int dayIndex, int startIndex, int endIndex, ActorRef<VenueResponse> replyTo) {
            this.dayIndex = dayIndex;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
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

    // availability[day][slot] true if occupied
    private final boolean[][] availability;

    public static Behavior<VenueCommand> create(Venue venue) {
        return Behaviors.setup(context -> new VenueActor(context, venue));
    }

    private VenueActor(ActorContext<VenueCommand> context, Venue venue) {
        super(context);
        this.venue = venue;
        availability = new boolean[DAYS][TIME_SLOTS_PER_DAY];
        for (boolean[] daySlots : availability) {
            Arrays.fill(daySlots, false);
        }
    }

    @Override
    public Receive<VenueCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(CheckAndAssignSlot.class, this::onCheckAndAssignSlot)
                .build();
    }

    private Behavior<VenueCommand> onCheckAndAssignSlot(CheckAndAssignSlot msg) {
        // Check availability
        for (int slot = msg.startIndex; slot < msg.endIndex; slot++) {
            if (availability[msg.dayIndex][slot]) {
                msg.replyTo.tell(new VenueRejected(venue, "Time slot occupied"));
                return this;
            }
        }
        // Assign
        for (int slot = msg.startIndex; slot < msg.endIndex; slot++) {
            availability[msg.dayIndex][slot] = true;
        }
        int durationSlots = msg.endIndex - msg.startIndex;
        msg.replyTo.tell(new VenueAccepted(venue, msg.dayIndex, msg.startIndex, durationSlots));
        return this;
    }
}

