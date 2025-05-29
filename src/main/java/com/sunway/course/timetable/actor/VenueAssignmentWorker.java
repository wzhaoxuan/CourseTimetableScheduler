package com.sunway.course.timetable.actor;
import akka.actor.typed.Behavior;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.*;
import akka.pattern.StatusReply;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.assignment.AssignmentResult;
import com.sunway.course.timetable.singleton.VenueAssignmentTable;
import java.util.List;


public class VenueAssignmentWorker extends AbstractBehavior<VenueAssignmentWorker.Command> {

    public interface Command {}

    private static class ConflictCheckResponse implements Command {
        final StatusReply<Boolean> reply;
        final Venue venue;

        ConflictCheckResponse(StatusReply<Boolean> reply, Venue venue) {
            this.reply = reply;
            this.venue = venue;
        }
    }

    public static Behavior<Command> create(Session session, List<Venue> venues,
                                           ActorRef<SubjectLecturerActor.Command> lecturerActor,
                                           ActorRef<StatusReply<AssignmentResult>> replyTo) {
        return Behaviors.setup(ctx -> new VenueAssignmentWorker(ctx, session, venues, lecturerActor, replyTo));
    }

    private final Session session;
    private final List<Venue> venues;
    private final ActorRef<SubjectLecturerActor.Command> lecturerActor;
    private final ActorRef<StatusReply<AssignmentResult>> replyTo;
    private int currentIndex = 0;

    private VenueAssignmentWorker(ActorContext<Command> ctx, Session session, List<Venue> venues,
                                  ActorRef<SubjectLecturerActor.Command> lecturerActor,
                                  ActorRef<StatusReply<AssignmentResult>> replyTo) {
        super(ctx);
        this.session = session;
        this.venues = venues;
        this.lecturerActor = lecturerActor;
        this.replyTo = replyTo;

        tryNextVenue(); // Start processing
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
            .onMessage(ConflictCheckResponse.class, this::onConflictCheckResponse)
            .build();
    }

    private void tryNextVenue() {
        if (currentIndex >= venues.size()) {
            replyTo.tell(StatusReply.success(new AssignmentResult(false, "No available venue found", session, null)));
            Behaviors.stopped();
            return;
        }

        Venue venue = venues.get(currentIndex++);
        boolean isAvailable = VenueAssignmentTable.getInstance().isAvailable(
                session.getDay(), session.getStartTime(), session.getEndTime(), venue);

        if (!isAvailable) {
            tryNextVenue(); // Skip and try next
            return;
        }

        // Create adapter to receive conflict check response
        ActorRef<StatusReply<Boolean>> conflictReplyTo = getContext().messageAdapter(
                (Class<StatusReply<Boolean>>) (Class<?>) StatusReply.class,
                reply -> new ConflictCheckResponse(reply, venue)
        );

        lecturerActor.tell(new SubjectLecturerActor.CheckConflict(session, conflictReplyTo));
    }

    private Behavior<Command> onConflictCheckResponse(ConflictCheckResponse msg) {
        boolean hasConflict = msg.reply.isSuccess() && msg.reply.getValue();

        if (hasConflict) {
            tryNextVenue();
        } else {
            VenueAssignmentTable.getInstance().assign(
                session.getDay(), session.getStartTime(), session.getEndTime(), msg.venue, session
            );

            replyTo.tell(StatusReply.success(
                new AssignmentResult(true, "Venue assigned successfully", session, msg.venue)
            ));
            return Behaviors.stopped();
        }

        return this;
    }
}

