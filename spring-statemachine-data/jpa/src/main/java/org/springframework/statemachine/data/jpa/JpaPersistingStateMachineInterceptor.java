/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.data.jpa;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.persist.AbstractPersistingStateMachineInterceptor;
import org.springframework.util.Assert;

/**
 * {@code JPA} implementation of a {@link AbstractPersistingStateMachineInterceptor}.
 *
 * @author Janne Valkealahti
 *
 */
public class JpaPersistingStateMachineInterceptor extends AbstractPersistingStateMachineInterceptor<String, String> {

	private final JpaRepositoryStateMachinePersist<String, String> persist;

	/**
	 * Instantiates a new jpa persisting state machine interceptor.
	 *
	 * @param jpaStateMachineRepository the jpa state machine repository
	 */
	public JpaPersistingStateMachineInterceptor(JpaStateMachineRepository jpaStateMachineRepository) {
		Assert.notNull(jpaStateMachineRepository, "'jpaStateMachineRepository' must be set");
		this.persist = new JpaRepositoryStateMachinePersist<String, String>(jpaStateMachineRepository);
	}

	/**
	 * Instantiates a new jpa persisting state machine interceptor.
	 *
	 * @param persist the persist
	 */
	public JpaPersistingStateMachineInterceptor(JpaRepositoryStateMachinePersist<String, String> persist) {
		Assert.notNull(persist, "'persist' must be set");
		this.persist = persist;
	}

	@Override
	protected void write(StateMachineContext<String, String> stateMachineContext) {
		try {
			persist.write(stateMachineContext, null);
		} catch (Exception e) {
			throw new StateMachineException("Unable to persist stateMachineContext", e);
		}
	}
}
