package com.sunway.course.timetable;
import org.springframework.stereotype.Component;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import com.sunway.course.timetable.actor.ReplyCollector;
import com.sunway.course.timetable.actor.SubjectLecturerActor;
import com.sunway.course.timetable.actor.VenueCoordinatorActor;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.assignment.AssignmentResult;

import akka.pattern.StatusReply;
import akka.actor.typed.Props;
import java.util.concurrent.TimeUnit;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Component
public class VenueAssignApp {
    public void runBlocking(String[] args) {
        List<Venue> venues = List.of(
            new Venue("Room 101", "Lecture Hall", 30, "Building A", "Floor 1"),
            new Venue("Room 102", "sdf", 50, "Building B", "Floor 2"),
            new Venue("Room 103", "Laboratory", 20, "Building C", "Floor 3")
        );

        ActorSystem<VenueCoordinatorActor.Command> system =
            ActorSystem.create(VenueCoordinatorActor.create(venues), "VenueCoordinator");

        Lecturer teacher = new Lecturer("Smith", "Dr", "sdf");

        Session session = new Session();
        session.setLecturer(teacher);
        session.setStartTime(LocalTime.of(9, 0));
        session.setEndTime(LocalTime.of(10, 0));
        session.setDay("Monday");
        session.setType("Lecture");
        session.setType_group("Lecture Group 1");

        List<Session> scheduledSessions = List.of(session);

        ActorRef<SubjectLecturerActor.Command> lecturer =
            system.systemActorOf(SubjectLecturerActor.create(scheduledSessions), "LecturerActor", Props.empty());

        CompletableFuture<AssignmentResult> future = new CompletableFuture<>();

        ActorRef<StatusReply<AssignmentResult>> replyCollector =
            system.systemActorOf(ReplyCollector.create(future), "ReplyCollector-" + session.getId(), Props.empty());

        system.tell(new VenueCoordinatorActor.RequestVenue(session, lecturer, replyCollector));

        future.orTimeout(5, TimeUnit.SECONDS)
            .thenAccept(result -> {
                    System.out.println("Venue assigned successfully: " + result.getAssignedVenue());
                    system.terminate();
            })
            .exceptionally(ex -> {
                System.err.println("Assignment Failed " + ex.getMessage());
                system.terminate();
                return null;
            });
    }
}
