package com.sunway.course.timetable.actor;


import java.util.concurrent.CompletableFuture;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.pattern.StatusReply;
import com.sunway.course.timetable.model.assignment.AssignmentResult;

/**
 * Temparary actor to capture the StatusReply<AssignmentResult>
 */

public class ReplyCollector extends AbstractBehavior<StatusReply<AssignmentResult>>{
    public static Behavior<StatusReply<AssignmentResult>> create(CompletableFuture<AssignmentResult> future) {
        return Behaviors.setup(ctx -> new ReplyCollector(ctx, future));
    }

    private final CompletableFuture<AssignmentResult> future;

    private ReplyCollector(ActorContext<StatusReply<AssignmentResult>> context, CompletableFuture<AssignmentResult> future) {
        super(context);
        this.future = future;
    }

     @Override
    public Receive<StatusReply<AssignmentResult>> createReceive() {
        return newReceiveBuilder()
                .onMessage(StatusReply.class, this::onReply)
                .build();
    }

    private Behavior<StatusReply<AssignmentResult>> onReply(StatusReply<AssignmentResult> reply) {
        if (reply.isSuccess()) {
            future.complete(reply.getValue());
        } else {
            future.completeExceptionally(new RuntimeException("Assignment failed"));
        }
        return Behaviors.stopped();
    }
}
