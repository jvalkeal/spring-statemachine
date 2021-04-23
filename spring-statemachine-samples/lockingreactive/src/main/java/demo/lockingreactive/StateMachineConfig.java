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
package demo.lockingreactive;

import java.time.Duration;
import java.util.function.Function;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.jdbc.lock.LockRepository;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.action.Action;
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
import org.springframework.statemachine.service.ReactiveLockingStateMachineHandlerService;

import reactor.core.publisher.Mono;

@Configuration
public class StateMachineConfig {

	private final static Logger log = LoggerFactory.getLogger(StateMachineConfig.class);

//tag::snippetA[]
	@Bean
	public StateMachineRuntimePersister<String, String, String> stateMachineRuntimePersister(
			JpaStateMachineRepository jpaStateMachineRepository) {
		return new JpaPersistingStateMachineInterceptor<>(jpaStateMachineRepository);
	}

	@Bean
	public LockRepository lockRepository(DataSource dataSource) {
		DefaultLockRepository repository = new DefaultLockRepository(dataSource, "demo");
		repository.setTimeToLive(60000);
		return repository;
	}

	@Bean
	public LockRegistry jdbcLockRegistry(LockRepository lockRepository) {
		return new JdbcLockRegistry(lockRepository);
	}

	@Bean
	public ReactiveLockingStateMachineHandlerService<String, String> reactiveLockingStateMachineHandlerService(
			LockRegistry lockRegistry, StateMachineFactory<String, String> stateMachineFactory,
			StateMachinePersist<String, String, String> stateMachinePersist) {
		return new IntegrationLockingStateMachineHandlerService<>(lockRegistry, stateMachineFactory,
				stateMachinePersist);
	}
//end::snippetA[]

//tag::snippetB[]
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
					.state("S2")
					.stateEntryFunction("S2", actionFunction())
					.state("S3")
					// .stateEntryFunction("S3", actionFunction())
					;
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					// .actionFunction(actionFunction())
					.and()
				.withExternal()
					.source("S2")
					.target("S3")
					.event("E2")
					// .actionFunction(actionFunction())
					.and()
				.withExternal()
					.source("S3")
					.target("S1")
					.event("E3")
					// .actionFunction(actionFunction())
					;
		}

		@Bean
		public Function<StateContext<String, String>, Mono<Void>> actionFunction() {
			return context -> {
				long sleep = 0;
				if (context.getMessageHeaders().containsKey("sleep")) {
					sleep = context.getMessageHeaders().get("sleep", Long.class);
				}
				log.info("Sleeping {}", sleep);
				return Mono.delay(Duration.ofMillis(sleep)).log().then().log();
			};
		}

		// @Bean
		// public Action<String, String> transitionAction() {
		// 	return context -> {
		// 		try {
		// 			log.info("Executing transitionAction");
		// 			Long sleep = context.getMessageHeaders().get("sleep", Long.class);
		// 			log.info("Executing transitionAction {}", sleep);
		// 			if (sleep != null) {
		// 				try {
		// 					log.info("Sleeping {}", sleep);
		// 					Thread.sleep(sleep);
		// 				} catch (Exception e) {
		// 					log.info("interrupt", e);
		// 					Thread.currentThread().interrupt();
		// 				}
		// 			}
		// 		} catch (Exception e) {
		// 			log.info("error", e);
		// 		}
		// 		// log.info("Executing transitionAction");
		// 		// Long sleep = context.getMessageHeaders().get("xxx", long.class);
		// 		// log.info("Executing transitionAction {}", sleep);
		// 		// if (sleep != null) {
		// 		// 	try {
		// 		// 		log.info("Sleeping {}", sleep);
		// 		// 		Thread.sleep(sleep);
		// 		// 	} catch (Exception e) {
		// 		// 		Thread.currentThread().interrupt();
		// 		// 	}
		// 		// }
		// 	};
		// }
	}
//end::snippetB[]
}
