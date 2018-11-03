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
import org.springframework.dsl.symboltable.ClassSymbol;
import org.springframework.dsl.symboltable.Scope;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.dsl.ssml.SsmlParser.ActionContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.ActionParameterContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.BeanIdContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.IdContext;
import org.springframework.statemachine.dsl.ssml.antlr.SsmlActionVisitor.SsmlActionResult;
import org.springframework.statemachine.dsl.ssml.support.SsmlSymbolTable;
import org.springframework.util.ClassUtils;

/**
 * {@code Visitor} visiting {@link Action} definitions.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
class SsmlActionVisitor<S, E> extends AbstractSsmlBaseVisitor<S, E, SsmlActionResult<S, E>> {

	private static final Log log = LogFactory.getLog(SsmlActionVisitor.class);

	SsmlActionVisitor(StateMachineComponentResolver<S, E> stateMachineComponentResolver,
			SsmlSymbolTable symbolTable, Scope scope) {
		super(stateMachineComponentResolver, symbolTable, scope);
	}

	@Override
	public SsmlActionResult<S, E> visitAction(ActionContext ctx) {
		IdContext id = ctx.id();
		if (id != null) {
			ClassSymbol classSymbol = new ClassSymbol(id.getText());
			classSymbol.setSuperClass(ClassUtils.getQualifiedName(Action.class));
			int len = id.getText().length();
			classSymbol.setRange(Range.from(id.getStart().getLine() - 1, id.getStart().getCharPositionInLine(),
					id.getStop().getLine() - 1, id.getStop().getCharPositionInLine() + len));
			getScope().define(classSymbol);
		}


		String action = ctx.id().getText();
		log.debug("visitAction " + action);

		String bean = null;
		for (ActionParameterContext parameterContext : ctx.actionParameters().actionParameter()) {
			if (parameterContext.actionType().BEAN() != null) {
				BeanIdContext beanId = parameterContext.actionType().beanId();
				if (beanId != null) {
					bean = beanId.getText();
				}
			}
		}

		return new SsmlActionResult<S, E>(action, getStateMachineComponentResolver().resolveAction(bean));
	}

	static class SsmlActionResult<S, E> {
		String id;
		Action<S, E> action;

		public SsmlActionResult(String id, Action<S, E> action) {
			this.id = id;
			this.action = action;
		}
	}
}
