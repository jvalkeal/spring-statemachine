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
package org.springframework.statemachine.buildtests;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.jdbc.lock.LockRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.data.jpa.JpaPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;
import org.springframework.statemachine.integration.IntegrationLockingStateMachineHandlerService;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.service.LockingStateMachineHandlerService;
import org.springframework.statemachine.service.ReactiveLockingStateMachineHandlerService;
import org.springframework.statemachine.state.State;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class JdbcIntegrationLockingStateMachineHandlerServiceTests extends AbstractBuildTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	@SuppressWarnings({"deprecation", "unchecked"})
	public void testLocking1() {
		context.register(Config.class, MachineConfig.class);
		context.refresh();

		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		JdbcLockRegistry lockRegistry = context.getBean(JdbcLockRegistry.class);
		StateMachinePersist<String, String, String> persist = context.getBean(StateMachinePersist.class);

		LockingStateMachineHandlerService<String, String> service = new IntegrationLockingStateMachineHandlerService<>(
			lockRegistry, stateMachineFactory, persist);

		State<String, String> state = service.handleWhileLocked("machine1", machine -> {
			machine.sendEvent("E1");
			return machine.getState();
		});
		assertThat(state).isNotNull();
		assertThat(state.getId()).isEqualTo("S2");

		state = service.handleWhileLocked("machine1", machine -> {
			machine.sendEvent("E2");
			return machine.getState();
		});
		assertThat(state).isNotNull();
		assertThat(state.getId()).isEqualTo("S3");
	}

	@Test
	@SuppressWarnings({"unchecked"})
	public void testLocking2() {
		context.register(Config.class, MachineConfig.class);
		context.refresh();

		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		JdbcLockRegistry lockRegistry = context.getBean(JdbcLockRegistry.class);
		StateMachinePersist<String, String, String> persist = context.getBean(StateMachinePersist.class);

		ReactiveLockingStateMachineHandlerService<String, String> service = new IntegrationLockingStateMachineHandlerService<>(
			lockRegistry, stateMachineFactory, persist);

		Mono<String> mono1 = service.handleReactivelyWhileLocked("machine2", machine -> {
			Mono<Message<String>> event = Mono.just(MessageBuilder.withPayload("E1").build());
			return machine.sendEvent(event)
				.then(Mono.fromCallable(() -> machine.getState().getId()));
		});
		StepVerifier.create(mono1).expectNext("S2").verifyComplete();

		Mono<String> mono2 = service.handleReactivelyWhileLocked("machine2", machine -> {
			Mono<Message<String>> event = Mono.just(MessageBuilder.withPayload("E2").build());
			return machine.sendEvent(event)
				.then(Mono.fromCallable(() -> machine.getState().getId()));
		});
		StepVerifier.create(mono2).expectNext("S3").verifyComplete();
	}

	@Configuration
	@EnableAutoConfiguration
	@EntityScan(basePackages = {"org.springframework.statemachine.data.jpa"})
	@EnableJpaRepositories(basePackages = {"org.springframework.statemachine.data.jpa"})
	static class Config {

		@Bean
		public StateMachineRuntimePersister<String, String, String> stateMachineRuntimePersister(
				JpaStateMachineRepository jpaStateMachineRepository) {
			return new JpaPersistingStateMachineInterceptor<>(jpaStateMachineRepository);
		}

		@Bean
		DefaultLockRepository defaultLockRepository(DataSource dataSource) {
			DefaultLockRepository repository = new DefaultLockRepository(dataSource, "fake");
			repository.setTimeToLive(60000);
			return repository;
		}

		@Bean
		JdbcLockRegistry jdbcLockRegistry(LockRepository lockRepository) {
			return new JdbcLockRegistry(lockRepository);
		}
	}

	@Configuration
	@EnableStateMachineFactory
	static class MachineConfig extends StateMachineConfigurerAdapter<String, String> {

		@Autowired
		private StateMachineRuntimePersister<String, String, String> stateMachineRuntimePersister;

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
			config
				.withPersistence()
					.runtimePersister(stateMachineRuntimePersister);
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S1")
					.state("S2")
					.state("S3");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.and()
				.withExternal()
					.source("S2")
					.target("S3")
					.event("E2");
		}
	}
}
