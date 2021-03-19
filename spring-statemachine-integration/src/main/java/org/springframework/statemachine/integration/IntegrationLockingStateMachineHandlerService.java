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
package org.springframework.statemachine.integration;

import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Publisher;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.service.LockingStateMachineHandlerService;
import org.springframework.statemachine.service.ReactiveLockingStateMachineHandlerService;
import org.springframework.util.Assert;

import reactor.core.publisher.Mono;

/**
 * Implementation of a {@link LockingStateMachineHandlerService} and
 * {@link ReactiveLockingStateMachineHandlerService} {@code spring-integration}
 * {@link LockRegistry} to lock machine while handling user function.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class IntegrationLockingStateMachineHandlerService<S, E>
		implements LockingStateMachineHandlerService<S, E>, ReactiveLockingStateMachineHandlerService<S, E> {

	private final static Log log = LogFactory.getLog(IntegrationLockingStateMachineHandlerService.class);
	private final LockRegistry lockRegistry;
	private final StateMachineFactory<S, E> stateMachineFactory;
	private final StateMachinePersist<S, E, String> stateMachinePersist;

	public IntegrationLockingStateMachineHandlerService(LockRegistry lockRegistry,
			StateMachineFactory<S, E> stateMachineFactory, StateMachinePersist<S, E, String> stateMachinePersist) {
		Assert.notNull(lockRegistry, "'lockRegistry' must be set");
		Assert.notNull(stateMachineFactory, "'stateMachineFactory' must be set");
		Assert.notNull(stateMachinePersist, "'stateMachinePersist' must be set");
		this.lockRegistry = lockRegistry;
		this.stateMachineFactory = stateMachineFactory;
		this.stateMachinePersist = stateMachinePersist;
	}

	@Override
    @SuppressWarnings({"deprecation"})
	public void handleWhileLocked(String machineId, Consumer<StateMachine<S, E>> machineConsumer) {
		Lock lock = this.lockRegistry.obtain(machineId);

		try {
			lock.lockInterruptibly();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new StateMachineException("Interrupted getting lock in the [" + this + ']', e);
		}

		try {
			StateMachine<S, E> stateMachine = stateMachineFactory.getStateMachine(machineId);
			StateMachineContext<S, E> stateMachineContext = stateMachinePersist.read(machineId);
			stateMachine = restoreStateMachine(stateMachine, stateMachineContext);
			stateMachine.start();
			machineConsumer.accept(stateMachine);
			stateMachine.stop();
		} catch (Exception e) {
			log.error("Error during locking processing", e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public <V> Mono<V> handleReactivelyWhileLocked(String machineId,
			Function<StateMachine<S, E>, Publisher<V>> machineFunction) {
		// build and restore machine
		Mono<StateMachine<S, E>> machineMono = Mono.fromSupplier(() -> {
				try {
					StateMachine<S, E> stateMachine = stateMachineFactory.getStateMachine(machineId);
					StateMachineContext<S, E> stateMachineContext = stateMachinePersist.read(machineId);
					stateMachine = restoreStateMachine(stateMachine, stateMachineContext);
					return stateMachine;
				} catch (Exception e) {
					throw new StateMachineException("Error restoring machine", e);
				}
			})
			.flatMap(m -> Mono.from(m.startReactively()).thenReturn(m));

		// lock are return ref
		Mono<MachineRef<S, E>> refMono = Mono.fromSupplier(() -> {
			Lock lock = this.lockRegistry.obtain(machineId);
			try {
				lock.lockInterruptibly();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new StateMachineException("Interrupted getting lock in the [" + this + ']', e);
			}
			return MachineRef.<S, E>from(machineMono, lock);
		});

		// Allow user to work with functions and clean things up
		return Mono.usingWhen(
			refMono,
			h -> machineMono.flatMap(m -> Mono.from(machineFunction.apply(m))),
			MachineRef::release,
			(h, error) -> h.release(),
			MachineRef::release
		);
	}

	private static class MachineRef<S, E> {
		Mono<StateMachine<S, E>> machine;
		Lock lock;
		MachineRef(Mono<StateMachine<S, E>> machine, Lock lock) {
			this.machine = machine.cache();
			this.lock = lock;
		}
		static <S, E> MachineRef<S, E> from(Mono<StateMachine<S, E>> machine, Lock lock) {
			return new MachineRef<>(machine, lock);
		}
		Mono<Void> release() {
			return machine.flatMap(m -> m.stopReactively().doOnTerminate(() -> {
				try {
					lock.unlock();
				} catch (Exception e) {
					log.error("Error during locking processing", e);
				}
			}));
		}
	}

	private StateMachine<S, E> restoreStateMachine(StateMachine<S, E> stateMachine,
			final StateMachineContext<S, E> stateMachineContext) {
		if (stateMachineContext == null) {
			return stateMachine;
		}
		stateMachine.getStateMachineAccessor().doWithRegion(function -> function.resetStateMachine(stateMachineContext));
		return stateMachine;
	}
}
