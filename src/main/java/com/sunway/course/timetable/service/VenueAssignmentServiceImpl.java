package com.sunway.course.timetable.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.assignment.AssignmentResult;
import com.sunway.course.timetable.model.venueAssignment.VenueAssignment;
import com.sunway.course.timetable.model.venueAssignment.VenueAssignmentId;
import com.sunway.course.timetable.repository.VenueAssignmentRepository;
import com.sunway.course.timetable.actor.SubjectLecturerActor;
import com.sunway.course.timetable.actor.VenueCoordinatorActor;
import com.sunway.course.timetable.actor.ReplyCollector;
import com.sunway.course.timetable.interfaces.services.VenueAssignmentService;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.pattern.StatusReply;
import akka.actor.typed.Props;


@Service
public class VenueAssignmentServiceImpl implements VenueAssignmentService {

    private final VenueAssignmentRepository venueAssignmentRepository;
    private final ActorSystem<VenueCoordinatorActor.Command> venueCoordinatorActor;
    private final ActorSystem<?> actorSystem;  // for ask pattern scheduling

    public VenueAssignmentServiceImpl(VenueAssignmentRepository repository,
                                      @Qualifier("venueCoordinatorActorSystem") ActorSystem<VenueCoordinatorActor.Command> venueCoordinatorActor,
                                      @Qualifier("generalActorSystem") ActorSystem<?> actorSystem) {
        this.venueAssignmentRepository = repository;
        this.venueCoordinatorActor = venueCoordinatorActor;
        this.actorSystem = actorSystem;
    }

    @Override
    public List<VenueAssignment> getAllAssignments() {
        return venueAssignmentRepository.findAll();
    }

    @Override
    public Optional<VenueAssignment> getAssignmentById(VenueAssignmentId key) {
        return venueAssignmentRepository.findById(key);
    }

    @Override
    public VenueAssignment saveAssignment(VenueAssignment assignment) {
        return venueAssignmentRepository.save(assignment);
    }

    @Override
    public void deleteAssignment(VenueAssignmentId key) {
        venueAssignmentRepository.deleteById(key);
    }

    // Assign a venue asynchronously using Akka and then save it
    public CompletionStage<AssignmentResult> assignVenue(Session session,
                                                     ActorRef<SubjectLecturerActor.Command> lecturerActor) {
    CompletableFuture<AssignmentResult> future = new CompletableFuture<>();

    // Use actorSystem to spawn a temporary actor for collecting the reply
    ActorRef<StatusReply<AssignmentResult>> replyCollectorRef =
        actorSystem.systemActorOf(ReplyCollector.create(future), "replyCollector-" + session.getId(), Props.empty());

    // Send request to venue coordinator
    venueCoordinatorActor.tell(
        new VenueCoordinatorActor.RequestVenue(session, lecturerActor, replyCollectorRef)
    );

    return future;
}
}
