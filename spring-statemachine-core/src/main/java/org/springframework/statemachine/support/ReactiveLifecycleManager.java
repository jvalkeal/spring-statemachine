/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.statemachine.support;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactiveLifecycleManager implements StateMachineReactiveLifecycle {

	private static final Log log = LogFactory.getLog(ReactiveLifecycleManager.class);
	private final AtomicEnum state = new AtomicEnum(LifecycleState.STOPPED);
	private EmitterProcessor<Mono<Void>> startRequestsProcessor;
	private EmitterProcessor<Mono<Void>> stopRequestsProcessor;
	private Flux<Mono<Void>> startRequests;
	private Flux<Mono<Void>> stopRequests;
	private Supplier<Mono<Void>> startRequest;
	private Supplier<Mono<Void>> stopRequest;

	public enum LifecycleState {
		STOPPED,
		STARTING,
		STARTED,
		STOPPING;
	}

	public ReactiveLifecycleManager(Supplier<Mono<Void>> startRequest, Supplier<Mono<Void>> stopRequest) {
		this.startRequest = startRequest;
		this.stopRequest = stopRequest;
		this.startRequestsProcessor = EmitterProcessor.<Mono<Void>>create(false);
		this.stopRequestsProcessor = EmitterProcessor.<Mono<Void>>create(false);
		this.startRequests = this.startRequestsProcessor.cache(1);
		this.stopRequests = this.stopRequestsProcessor.cache(1);
	}

	@Override
	public Mono<Void> startReactively() {
		log.debug("Request startReactively " + this);
		return Mono.defer(() -> {
			return Mono.just(state.compareAndSet(LifecycleState.STOPPED, LifecycleState.STARTING))
				.filter(owns -> owns)
				.flatMap(owns -> this.startRequests.next().flatMap(Function.identity()).doOnSuccess(aVoid -> {
					state.set(LifecycleState.STARTED);
				}))
				.then()
				;
		})
		;
	}

	@Override
	public Mono<Void> stopReactively() {
		log.debug("Request stopReactively " + this);
		return Mono.defer(() -> {
			return Mono.just(state.compareAndSet(LifecycleState.STARTED, LifecycleState.STOPPING))
				.filter(owns -> owns)
				.flatMap(owns -> this.stopRequests.next().flatMap(Function.identity()).doOnSuccess(aVoid -> {
					state.set(LifecycleState.STOPPED);
				}))
				.then()
				;
		})
		;
	}

	public LifecycleState getLifecycleState() {
		return state.get();
	}

	public boolean isRunning() {
		return state.get() == LifecycleState.STARTED;
	}

	protected Mono<Void> doStartReactively() {
		return Mono.empty();
	}

	protected Mono<Void> doStopReactively() {
		return Mono.empty();
	}

	private class AtomicEnum {

		private final AtomicReference<LifecycleState> ref;

		public AtomicEnum(final LifecycleState initialValue) {
			this.ref = new AtomicReference<LifecycleState>(initialValue);
		}

		public void set(final LifecycleState newValue) {
			log.info("Lifecycle to " + newValue + " in " + ReactiveLifecycleManager.this);
			this.ref.set(newValue);
		}

		public LifecycleState get() {
			return this.ref.get();
		}

		public boolean compareAndSet(final LifecycleState expect, final LifecycleState update) {
			boolean set = this.ref.compareAndSet(expect, update);
			if (set) {
				log.info("Lifecycle from " + expect + " to " + update + " in " + ReactiveLifecycleManager.this);
				if (update == LifecycleState.STARTING) {
					log.info("Posting on next doStartReactively(");
					startRequestsProcessor.onNext(startRequest.get());
				} else if (update == LifecycleState.STOPPING) {
					stopRequestsProcessor.onNext(stopRequest.get());
				}
			}
			return set;
		}
	}
}
