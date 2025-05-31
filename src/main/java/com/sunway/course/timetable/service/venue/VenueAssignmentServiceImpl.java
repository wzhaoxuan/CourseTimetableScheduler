package com.sunway.course.timetable.service.venue;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.interfaces.services.VenueAssignmentService;
import com.sunway.course.timetable.model.venueAssignment.VenueAssignment;
import com.sunway.course.timetable.model.venueAssignment.VenueAssignmentId;
import com.sunway.course.timetable.repository.VenueAssignmentRepository;


@Service
public class VenueAssignmentServiceImpl implements VenueAssignmentService {

    private final VenueAssignmentRepository venueAssignmentRepository;
    // private final ActorSystem<VenueCoordinatorActor.Command> venueCoordinatorActor;
    // private final ActorSystem<?> actorSystem;  // for ask pattern scheduling

    public VenueAssignmentServiceImpl(VenueAssignmentRepository repository) {
        this.venueAssignmentRepository = repository;
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

//     // Assign a venue asynchronously using Akka and then save it
//     public CompletionStage<AssignmentResult> assignVenue(Session session,
//                                                      ActorRef<SubjectLecturerActor.Command> lecturerActor) {
//     CompletableFuture<AssignmentResult> future = new CompletableFuture<>();

//     // Use actorSystem to spawn a temporary actor for collecting the reply
//     ActorRef<StatusReply<AssignmentResult>> replyCollectorRef =
//         actorSystem.systemActorOf(ReplyCollector.create(future), "replyCollector-" + session.getId(), Props.empty());

//     // Send request to venue coordinator
//     venueCoordinatorActor.tell(
//         new VenueCoordinatorActor.RequestVenue(session, lecturerActor, replyCollectorRef)
//     );

//     return future;
// }
}
