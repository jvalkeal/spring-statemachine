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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.springframework.statemachine.support.ReactiveLifecycleManager.LifecycleState;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class ReactiveLifecycleManagerTests {

	@Test
	public void testStartStop() {
		ReactiveLifecycleManager manager = new NoopReactiveLifecycleManager();

		assertThat(manager.isRunning(), is(false));
		assertThat(manager.getLifecycleState(), is(LifecycleState.STOPPED));

		StepVerifier.create(manager.startReactively()).expectComplete().verify();
		assertThat(manager.isRunning(), is(true));
		assertThat(manager.getLifecycleState(), is(LifecycleState.STARTED));

		StepVerifier.create(manager.stopReactively()).expectComplete().verify();
		assertThat(manager.isRunning(), is(false));
		assertThat(manager.getLifecycleState(), is(LifecycleState.STOPPED));

		StepVerifier.create(manager.startReactively()).expectComplete().verify();
		assertThat(manager.isRunning(), is(true));
		assertThat(manager.getLifecycleState(), is(LifecycleState.STARTED));

		StepVerifier.create(manager.stopReactively()).expectComplete().verify();
		assertThat(manager.isRunning(), is(false));
		assertThat(manager.getLifecycleState(), is(LifecycleState.STOPPED));
	}

	@Test
	public void testStartRecursive() {
		ReactiveLifecycleManager manager = new RecursiveStartReactiveLifecycleManager();
		StepVerifier.create(manager.startReactively()).expectComplete().verify();
		assertThat(manager.isRunning(), is(true));
		assertThat(manager.getLifecycleState(), is(LifecycleState.STARTED));
	}

	private static class NoopReactiveLifecycleManager extends ReactiveLifecycleManager {

		@Override
		protected Mono<Void> doStartReactively() {
			return Mono.<Void>empty().doOnEach(System.out::println);
		}
	}

	private static class RecursiveStartReactiveLifecycleManager extends ReactiveLifecycleManager {

		private final AtomicBoolean recursive = new AtomicBoolean(true);

		@Override
		protected Mono<Void> doStartReactively() {
			if (recursive.compareAndSet(true, false)) {
				return startReactively();
			} else {
				return Mono.empty();
			}
		}
	}

	private static class SlowStartReactiveLifecycleManager extends ReactiveLifecycleManager {

		@Override
		protected Mono<Void> doStartReactively() {
			return Mono.delay(Duration.ofSeconds(2)).then();
		}
	}
}
