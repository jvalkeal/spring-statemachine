package org.springframework.statemachine.access;

import org.springframework.statemachine.StateMachineContext;

import reactor.core.publisher.Mono;

public interface ReactiveStateMachineAccess<S, E> {

	Mono<Void> resetStateMachineReactively(StateMachineContext<S, E> stateMachineContext);
}
