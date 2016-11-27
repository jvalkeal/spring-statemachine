package org.springframework.statemachine.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Processor;
import org.springframework.context.Lifecycle;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.StateContext.Stage;
import org.springframework.statemachine.state.JoinPseudoState;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineExecutor.StateMachineExecutorTransit;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.trigger.DefaultTriggerContext;
import org.springframework.statemachine.trigger.TimerTrigger;
import org.springframework.statemachine.trigger.Trigger;
import org.springframework.statemachine.trigger.TriggerListener;

import reactor.core.publisher.BlockingSink;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.WorkQueueProcessor;

public class ReactiveStateMachineExecutor<S, E> extends LifecycleObjectSupport implements StateMachineExecutor<S, E> {

	private static final Log log = LogFactory.getLog(ReactiveStateMachineExecutor.class);

	private StateMachineExecutorTransit<S, E> stateMachineExecutorTransit;
	private final AtomicBoolean initialHandled = new AtomicBoolean(false);

	private final StateMachine<S, E> stateMachine;
	private final StateMachine<S, E> relayStateMachine;
	private final Map<Trigger<S, E>, Transition<S,E>> triggerToTransitionMap;
	private final List<Transition<S, E>> triggerlessTransitions;
	private final Collection<Transition<S,E>> transitions;
	private final Transition<S, E> initialTransition;
	private final Message<E> initialEvent;

	private final Set<Transition<S, E>> joinSyncTransitions = new HashSet<>();
	private final Set<State<S, E>> joinSyncStates = new HashSet<>();

	private final StateMachineInterceptorList<S, E> interceptors =
			new StateMachineInterceptorList<S, E>();

	private volatile Message<E> forwardedInitialEvent;

	private final List<Message<E>> deferList = new ArrayList<Message<E>>();

//	private Processor<TriggerQueueItem, TriggerQueueItem> triggerProcessor = WorkQueueProcessor.create();
//	private Processor<Flux<Message<E>>, Flux<Message<E>>> eventProcessor = WorkQueueProcessor.create();
	private EmitterProcessor<TriggerQueueItem> triggerProcessor = EmitterProcessor.create();
	private EmitterProcessor<Flux<Message<E>>> eventProcessor = EmitterProcessor.create();
	private BlockingSink<TriggerQueueItem> triggerProcessorSink;
	private BlockingSink<Flux<Message<E>>> eventProcessorSink;

	public ReactiveStateMachineExecutor(StateMachine<S, E> stateMachine, StateMachine<S, E> relayStateMachine,
			Collection<Transition<S, E>> transitions, Map<Trigger<S, E>, Transition<S, E>> triggerToTransitionMap,
			List<Transition<S, E>> triggerlessTransitions, Transition<S, E> initialTransition, Message<E> initialEvent) {
		this.stateMachine = stateMachine;
		this.relayStateMachine = relayStateMachine;
		this.triggerToTransitionMap = triggerToTransitionMap;
		this.triggerlessTransitions = triggerlessTransitions;
		this.transitions = transitions;
		this.initialTransition = initialTransition;
		this.initialEvent = initialEvent;
		registerTriggerListener();
	}

	@Override
	protected void onInit() throws Exception {
		super.onInit();
		triggerProcessorSink = triggerProcessor.connectSink();
		eventProcessorSink = eventProcessor.connectSink();
	}

	@Override
	public void queueEvent(Message<E> message) {
		Flux<Message<E>> deferFlux = Flux.fromIterable(deferList);
		Flux<Message<E>> messageFlux = Flux.just(message);
//		eventProcessor.onNext(Flux.merge(messageFlux, deferFlux));
		eventProcessorSink.next(Flux.merge(messageFlux, deferFlux));
	}

	@Override
	public void queueTrigger(Trigger<S, E> trigger, Message<E> message) {
//		triggerProcessor.onNext(new TriggerQueueItem(trigger, message));
		triggerProcessorSink.next(new TriggerQueueItem(trigger, message));
	}

	@Override
	public void queueDeferredEvent(Message<E> message) {
		deferList.add(message);
	}

	@Override
	public void execute() {
	}

	@Override
	public void setInitialEnabled(boolean enabled) {
		initialHandled.set(!enabled);
	}

	@Override
	public void doStart() {
		super.doStart();
		Flux.from(eventProcessor).doOnNext(flux -> {
			flux.doOnNext(message -> {
				handleEvent(message);
			}).subscribe();
		}).subscribe();

		Flux.from(triggerProcessor).doOnNext(item -> {
			handleTrigger(item);
		}).subscribe();

		if (!initialHandled.getAndSet(true)) {
			ArrayList<Transition<S, E>> trans = new ArrayList<Transition<S, E>>();
			trans.add(initialTransition);
			// TODO: should we merge if initial event is actually used?
			if (initialEvent != null) {
				handleInitialTrans(initialTransition, initialEvent);
			} else {
				handleInitialTrans(initialTransition, forwardedInitialEvent);
			}
//			return;
		}

	}

	private void handleInitialTrans(Transition<S, E> tran, Message<E> queuedMessage) {
		StateContext<S, E> stateContext = buildStateContext(queuedMessage, tran, relayStateMachine);
		tran.transit(stateContext);
		stateMachineExecutorTransit.transit(tran, stateContext, queuedMessage);
	}

	private void handleEvent(Message<E> message) {
		State<S,E> currentState = stateMachine.getState();
		if ((currentState != null && currentState.shouldDefer(message))) {
			log.info("Current state " + currentState + " deferred event " + message);
			queueDeferredEvent(message);
			return;
		}
		for (Transition<S,E> transition : transitions) {
			State<S,E> source = transition.getSource();
			Trigger<S, E> trigger = transition.getTrigger();

			if (StateMachineUtils.containsAtleastOne(source.getIds(), currentState.getIds())) {
				if (trigger != null && trigger.evaluate(new DefaultTriggerContext<S, E>(message.getPayload()))) {
					queueTrigger(trigger, message);
					return;
				}
			}
		}
	}

	private void handleTrigger(TriggerQueueItem queueItem) {
		Message<E> queuedMessage = null;
		State<S,E> currentState = stateMachine.getState();
		if (queueItem != null && currentState != null) {
//			if (log.isDebugEnabled()) {
//				log.debug("Process trigger item " + queueItem + " " + this);
//			}
			// queued message is kept on a class level order to let
			// triggerless transition to receive this message if it doesn't
			// kick in in this poll loop.
			queuedMessage = queueItem.message;
			E event = queuedMessage != null ? queuedMessage.getPayload() : null;

			// need all transitions trigger could match, event trigger may match
			// multiple
			// need to go up from substates and ask if trigger transit, if not
			// check super
			ArrayList<Transition<S, E>> trans = new ArrayList<Transition<S, E>>();

			if (event != null) {
				ArrayList<S> ids = new ArrayList<S>(currentState.getIds());
				Collections.reverse(ids);
				for (S id : ids) {
					for (Entry<Trigger<S, E>, Transition<S, E>> e : triggerToTransitionMap.entrySet()) {
						Trigger<S, E> tri = e.getKey();
						E ee = tri.getEvent();
						Transition<S, E> tra = e.getValue();
						if (event.equals(ee)) {
							if (tra.getSource().getId().equals(id) && !trans.contains(tra)) {
								trans.add(tra);
								continue;
							}
						}
					}
				}
			}

			// most likely timer
			if (trans.isEmpty()) {
				trans.add(triggerToTransitionMap.get(queueItem.trigger));
			}

			// go through candidates and transit max one
			handleTriggerTrans(trans, queuedMessage);
		}
		if (stateMachine.getState() != null) {
			// loop triggerless transitions here so that
			// all "chained" transitions will get queue message
			boolean transit = false;
			do {
				transit = handleTriggerTrans(triggerlessTransitions, queuedMessage);
			} while (transit);
		}

	}

	private boolean handleTriggerTrans(List<Transition<S, E>> trans, Message<E> queuedMessage) {
		boolean transit = false;
		for (Transition<S, E> t : trans) {
			if (t == null) {
				continue;
			}
			State<S,E> source = t.getSource();
			if (source == null) {
				continue;
			}
			State<S,E> currentState = stateMachine.getState();
			if (currentState == null) {
				continue;
			}
			if (!StateMachineUtils.containsAtleastOne(source.getIds(), currentState.getIds())) {
				continue;
			}

			// special handling of join
			if (StateMachineUtils.isPseudoState(t.getTarget(), PseudoStateKind.JOIN)) {
				if (joinSyncStates.isEmpty()) {
					List<State<S, E>> joins = ((JoinPseudoState<S, E>)t.getTarget().getPseudoState()).getJoins();
					joinSyncStates.addAll(joins);
				}
				joinSyncTransitions.add(t);
				boolean removed = joinSyncStates.remove(t.getSource());
				boolean joincomplete = removed & joinSyncStates.isEmpty();
				if (joincomplete) {
					for (Transition<S, E> tt : joinSyncTransitions) {
						StateContext<S, E> stateContext = buildStateContext(queuedMessage, tt, relayStateMachine);
						tt.transit(stateContext);
						stateMachineExecutorTransit.transit(tt, stateContext, queuedMessage);
					}
					joinSyncTransitions.clear();
					break;
				} else {
					continue;
				}
			}

			StateContext<S, E> stateContext = buildStateContext(queuedMessage, t, relayStateMachine);
			try {
				stateContext = interceptors.preTransition(stateContext);
			} catch (Exception e) {
				// currently expect that if exception is
				// thrown, this transition will not match.
				// i.e. security may throw AccessDeniedException
				log.info("Interceptors threw exception", e);
				stateContext = null;
			}
			if (stateContext == null) {
				break;
			}

			try {
				transit = t.transit(stateContext);
			} catch (Exception e) {
				log.warn("Transition " + t + " caused error " + e);
			}
			if (transit) {
				stateMachineExecutorTransit.transit(t, stateContext, queuedMessage);
				interceptors.postTransition(stateContext);
				break;
			}
		}
		return transit;
	}

	@Override
	public void doStop() {
		super.doStop();
		initialHandled.set(false);
	}

	@Override
	public void setForwardedInitialEvent(Message<E> message) {
		forwardedInitialEvent = message;
	}

	@Override
	public void setStateMachineExecutorTransit(StateMachineExecutorTransit<S, E> stateMachineExecutorTransit) {
		this.stateMachineExecutorTransit = stateMachineExecutorTransit;
	}

	@Override
	public void addStateMachineInterceptor(StateMachineInterceptor<S, E> interceptor) {
		interceptors.add(interceptor);
	}

	private StateContext<S, E> buildStateContext(Message<E> message, Transition<S,E> transition, StateMachine<S, E> stateMachine) {
		// TODO: maybe a direct use of MessageHeaders is wring, combine
		//       payload and headers as a message?

		// add sm id to headers so that user of a StateContext can
		// see who initiated this transition
		MessageHeaders messageHeaders = message != null ? message.getHeaders() : new MessageHeaders(
				new HashMap<String, Object>());
		Map<String, Object> map = new HashMap<String, Object>(messageHeaders);
		if (!map.containsKey(StateMachineSystemConstants.STATEMACHINE_IDENTIFIER)) {
			// don't set sm id if it's already present because
			// we want to keep the originating sm id
			map.put(StateMachineSystemConstants.STATEMACHINE_IDENTIFIER, stateMachine.getUuid());
		}
		return new DefaultStateContext<S, E>(Stage.TRANSITION, message, new MessageHeaders(map), stateMachine.getExtendedState(), transition, stateMachine, null, null, null);
	}

	private void registerTriggerListener() {
		for (final Trigger<S, E> trigger : triggerToTransitionMap.keySet()) {
			if (trigger instanceof TimerTrigger) {
				((TimerTrigger<?, ?>) trigger).addTriggerListener(new TriggerListener() {
					@Override
					public void triggered() {
						if (log.isDebugEnabled()) {
							log.debug("TimedTrigger triggered " + trigger);
						}
						queueTrigger(trigger, null);
					}
				});
			}
			if (trigger instanceof Lifecycle) {
				((Lifecycle) trigger).start();
			}
		}
	}

	private class TriggerQueueItem {
		Trigger<S, E> trigger;
		Message<E> message;
		public TriggerQueueItem(Trigger<S, E> trigger, Message<E> message) {
			this.trigger = trigger;
			this.message = message;
		}
		@Override
		public String toString() {
			return "TriggerItem [trigger=" + trigger + ", message=" + message + "]";
		}
	}

}
