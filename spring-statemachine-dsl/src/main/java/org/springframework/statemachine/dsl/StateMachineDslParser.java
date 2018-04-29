/*
 * Copyright 2018 the original author or authors.
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
package org.springframework.statemachine.dsl;

import org.springframework.dsl.DslParser;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;

/**
 * Extension of a {@link DslParser} providing {@link StateMachineComponentResolver}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 * @param <T> the type of {@link DslParserResult} value
 *
 */
public interface StateMachineDslParser<S, E, T> extends DslParser<T> {

	/**
	 * Sets the state machine component resolver.
	 *
	 * @param resolver the resolver
	 */
	void setStateMachineComponentResolver(StateMachineComponentResolver<S, E> resolver);
}
