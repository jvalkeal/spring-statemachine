/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.service;

import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.springframework.statemachine.StateMachine;

import reactor.core.publisher.Mono;

/**
 * Reactive interface for a service type which handles one stop shop with a
 * statemachine lifecycle by handling machine by first starting it, waiting
 * caller to do something with it and then closing it.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface ReactiveLockingStateMachineHandlerService<S, E> {

	/**
	 * Handle machine while locked.
	 *
	 * @param <V> type of a returned mono
	 * @param machineId the machine id
	 * @param machineFunction function for handling machine
	 * @return mono for completion
	 */
	<V> Mono<V> handleReactivelyWhileLocked(String machineId, Function<StateMachine<S, E>, Publisher<V>> machineFunction);
}
