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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dsl.domain.Range;
import org.springframework.dsl.symboltable.Scope;
import org.springframework.dsl.symboltable.model.ClassSymbol;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.dsl.ssml.SsmlParser.BeanIdContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.GuardContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.GuardParameterContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.IdContext;
import org.springframework.statemachine.dsl.ssml.antlr.SsmlGuardVisitor.SsmlGuardResult;
import org.springframework.statemachine.dsl.ssml.support.SsmlSymbolTable;
import org.springframework.statemachine.guard.Guard;
import org.springframework.util.ClassUtils;

/**
 * {@code Visitor} visiting {@link Guard} definitions.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
class SsmlGuardVisitor<S, E> extends AbstractSsmlBaseVisitor<S, E, SsmlGuardResult<S, E>> {

	private static final Log log = LogFactory.getLog(SsmlGuardVisitor.class);

	SsmlGuardVisitor(StateMachineComponentResolver<S, E> stateMachineComponentResolver,
			SsmlSymbolTable symbolTable, Scope scope) {
		super(stateMachineComponentResolver, symbolTable, scope);
	}

	@Override
	public SsmlGuardResult<S, E> visitGuard(GuardContext ctx) {
		IdContext id = ctx.id();
		if (id != null) {
			ClassSymbol classSymbol = new ClassSymbol(id.getText());
			classSymbol.setSuperClass(ClassUtils.getQualifiedName(Guard.class));
			int len = id.getText().length();
			classSymbol.setRange(Range.from(id.getStart().getLine() - 1, id.getStart().getCharPositionInLine(),
					id.getStop().getLine() - 1, id.getStop().getCharPositionInLine() + len));
			getScope().define(classSymbol);
		}

		String guard = ctx.id().getText();
		log.debug("visitGuard "+ guard);

		String bean = null;
		for (GuardParameterContext parameterContext : ctx.guardParameters().guardParameter()) {

			if (parameterContext.guardType().BEAN() != null) {
				BeanIdContext beanId = parameterContext.guardType().beanId();
				if (beanId != null) {
					bean = beanId.getText();
				}
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
