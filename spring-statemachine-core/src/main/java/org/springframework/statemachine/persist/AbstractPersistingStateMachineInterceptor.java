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
package org.springframework.statemachine.persist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.state.AbstractState;
import org.springframework.statemachine.state.HistoryPseudoState;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.AbstractStateMachine;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;

/**
 * Base class for {@link StateMachineInterceptor} persisting {@link StateMachineContext}s.
 * This class is to be used as a base implementation which wants to persist a machine which
 * is about to kept running as normal use case for persistence is to stop machine, persist and
 * then start it again.
 *
 * @author Janne Valkealahti
 *
 */
public abstract class AbstractPersistingStateMachineInterceptor<S, E> extends StateMachineInterceptorAdapter<S, E> {

	@Override
	public void preStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition, StateMachine<S, E> stateMachine) {
		// try to persist context and in case of failure, interceptor
		// call chain aborts transition
		// TODO: should probably come up with a policy vs. not force feeding this functionality
		write(buildStateMachineContext(stateMachine, state));
	}

	@Override
	public void postStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition, StateMachine<S, E> stateMachine) {
		// initial transitions are never intercepted as those cannot fail or get aborted.
		// for now, handle persistence in post state change
		// TODO: consider intercept initial transition, but not aborting if error is thrown?
		if (state != null && transition != null && transition.getKind() == TransitionKind.INITIAL) {
			write(buildStateMachineContext(stateMachine, state));
		}
	}

	/**
	 * Write {@link StateMachineContext} into persistent store.
	 *
	 * @param stateMachineContext the state machine context
	 */
	protected abstract void write(StateMachineContext<S, E> stateMachineContext);

	/**
	 * Builds the state machine context.
	 *
	 * @param stateMachine the state machine
	 * @param state the state
	 * @return the state machine context
	 */
	protected StateMachineContext<S, E> buildStateMachineContext(StateMachine<S, E> stateMachine, State<S, E> state) {
		ExtendedState extendedState = new DefaultExtendedState();
		extendedState.getVariables().putAll(stateMachine.getExtendedState().getVariables());

		ArrayList<StateMachineContext<S, E>> childs = new ArrayList<StateMachineContext<S, E>>();
		S id = null;
		if (state.isSubmachineState()) {
			id = getDeepState(state);
		} else if (state.isOrthogonal()) {
			Collection<Region<S, E>> regions = ((AbstractState<S, E>)state).getRegions();
			for (Region<S, E> r : regions) {
				StateMachine<S, E> rsm = (StateMachine<S, E>) r;
				childs.add(buildStateMachineContext(rsm, state));
			}
			id = state.getId();
		} else {
			id = state.getId();
		}

		// building history state mappings
		Map<S, S> historyStates = new HashMap<S, S>();
		PseudoState<S, E> historyState = ((AbstractStateMachine<S, E>) stateMachine).getHistoryState();
		if (historyState != null) {
			historyStates.put(null, ((HistoryPseudoState<S, E>)historyState).getState().getId());
		}
		Collection<State<S, E>> states = stateMachine.getStates();
		for (State<S, E> ss : states) {
			if (ss.isSubmachineState()) {
				StateMachine<S, E> submachine = ((AbstractState<S, E>) ss).getSubmachine();
				PseudoState<S, E> ps = ((AbstractStateMachine<S, E>) submachine).getHistoryState();
				if (ps != null) {
					State<S, E> pss = ((HistoryPseudoState<S, E>)ps).getState();
					if (pss != null) {
						historyStates.put(ss.getId(), pss.getId());
					}
				}
			}
		}
		return new DefaultStateMachineContext<S, E>(childs, id, null, null, extendedState, historyStates, stateMachine.getId());
	}

	private S getDeepState(State<S, E> state) {
		Collection<S> ids1 = state.getIds();
		@SuppressWarnings("unchecked")
		S[] ids2 = (S[]) ids1.toArray();
		// TODO: can this be empty as then we'd get error?
		return ids2[ids2.length-1];
	}
}
