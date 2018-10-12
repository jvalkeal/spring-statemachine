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
package org.springframework.statemachine.dsl.ssml.antlr;

import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.dsl.ssml.SsmlParser;
import org.springframework.statemachine.dsl.ssml.SsmlParser.GuardContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.GuardParameterContext;
import org.springframework.statemachine.dsl.ssml.antlr.SsmlGuardVisitor.SsmlGuardResult;
import org.springframework.statemachine.guard.Guard;

/**
 * {@code Visitor} visiting {@link Guard} definitions.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
class SsmlGuardVisitor<S, E> extends AbstractSsmlBaseVisitor<S, E, SsmlGuardResult<S, E>> {

	SsmlGuardVisitor(StateMachineComponentResolver<S, E> stateMachineComponentResolver) {
		super(stateMachineComponentResolver);
	}

	@Override
	public SsmlGuardResult<S, E> visitGuard(GuardContext ctx) {
		String guard = ctx.id().getText();

		String bean = null;
		for (GuardParameterContext parameterContext : ctx.guardParameters().guardParameter()) {
			if (parameterContext.guardType().BEAN() != null) {
				bean = parameterContext.id().getText();
			}
		}

		return new SsmlGuardResult<S, E>(guard, getStateMachineComponentResolver().resolveGuard(bean));
	}

	static class SsmlGuardResult<S, E> {
		String id;
		Guard<S, E> guard;

		public SsmlGuardResult(String id, Guard<S, E> guard) {
			this.id = id;
			this.guard = guard;
		}
	}
}
