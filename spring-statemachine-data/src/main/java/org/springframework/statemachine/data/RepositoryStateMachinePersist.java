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
package org.springframework.statemachine.data;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.kryo.KryoSerialisationService;

/**
 * Base implementation of a {@link StateMachinePersist} using Spring Data Repositories.
 *
 * @author Janne Valkealahti
 *
 */
public abstract class RepositoryStateMachinePersist<M extends RepositoryStateMachine, S, E> implements StateMachinePersist<S, E, Object> {

	private final KryoSerialisationService serialisationService = new KryoSerialisationService();

	@Override
	public void write(StateMachineContext<S, E> context, Object contextObj) throws Exception {
		M build = build(context, serialisationService.serializeStateMachineContext(context));
		getRepository().save(build);
	}

	@Override
	public StateMachineContext<S, E> read(Object contextObj) throws Exception {
		M repositoryStateMachine = getRepository().findOne(contextObj.toString());
		if (repositoryStateMachine != null) {
			return serialisationService.deserializeStateMachineContext(repositoryStateMachine.getStateMachineContext());
		}
		return null;
	}

	/**
	 * Gets the repository.
	 *
	 * @return the repository
	 */
	protected abstract StateMachineRepository<M> getRepository();

	/**
	 * Builds the generic {@link RepositoryStateMachine} entity.
	 *
	 * @param context the context
	 * @param serialisedContext the serialised context
	 * @return the repository state machine entity
	 */
	protected abstract M build(StateMachineContext<S, E> context, byte[] serialisedContext);

}
