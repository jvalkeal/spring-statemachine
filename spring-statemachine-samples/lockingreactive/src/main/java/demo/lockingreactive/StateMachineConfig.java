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

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.jdbc.lock.LockRepository;
import org.springframework.integration.support.locks.LockRegistry;
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
import org.springframework.statemachine.service.ReactiveLockingStateMachineHandlerService;

@Configuration
public class StateMachineConfig {

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
					.event("E2")
					.and()
				.withExternal()
					.source("S3")
					.target("S1")
					.event("E3");
		}
	}
//end::snippetB[]
}
