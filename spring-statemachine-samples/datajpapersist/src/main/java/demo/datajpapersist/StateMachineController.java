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
package demo.datajpapersist;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.data.RepositoryTransition;
import org.springframework.statemachine.data.TransitionRepository;
import org.springframework.statemachine.data.jpa.JpaPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.jpa.JpaRepositoryStateMachinePersist;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StateMachineController {

	public final static String MACHINE_ID_1 = "datajpapersist1";
	public final static String MACHINE_ID_2 = "datajpapersist2";
	private final static String[] MACHINES = new String[] { MACHINE_ID_1, MACHINE_ID_2 };

	@Autowired
	private StateMachineFactory<String, String> stateMachineFactory;

	@Autowired
	private TransitionRepository<? extends RepositoryTransition> transitionRepository;

	@Autowired
	private JpaRepositoryStateMachinePersist<String, String> jpaRepositoryStateMachinePersist;

	private StateMachine<String, String> cachedStateMachine;
	private final StateMachineLogListener listener = new StateMachineLogListener();

	@RequestMapping("/")
	public String home() {
		return "redirect:/state";
	}

	@RequestMapping("/state")
	public String feedAndGetStates(
			@RequestParam(value = "events", required = false) List<String> events,
			@RequestParam(value = "machine", required = false, defaultValue = MACHINE_ID_1) String machine,
			Model model) throws Exception {

		StateMachine<String, String> stateMachine = getStateMachine(machine);
		if (events != null) {
			for (String event : events) {
				stateMachine.sendEvent(event);
			}
		}
		StateMachineContext<String, String> stateMachineContext = jpaRepositoryStateMachinePersist.read(machine);
		model.addAttribute("allMachines", MACHINES);
		model.addAttribute("machine", machine);
		model.addAttribute("allEvents", getEvents());
		model.addAttribute("messages", createMessages(listener.getMessages()));
		model.addAttribute("context", stateMachineContext != null ? stateMachineContext.toString() : "");
		return "states";
	}

	private synchronized StateMachine<String, String> getStateMachine(String machineId) throws Exception {
		if (cachedStateMachine == null) {
			cachedStateMachine = buildStateMachine(machineId);
			cachedStateMachine.start();
		} else {
			if (!ObjectUtils.nullSafeEquals(cachedStateMachine.getId(), machineId)) {
				cachedStateMachine.stop();
				cachedStateMachine = buildStateMachine(machineId);
				cachedStateMachine.start();
			}
		}
		return cachedStateMachine;
	}

	private StateMachine<String, String> buildStateMachine(String machineId) throws Exception {
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine(machineId);
		stateMachine.addStateListener(listener);

		final JpaPersistingStateMachineInterceptor interceptor =
				new JpaPersistingStateMachineInterceptor(jpaRepositoryStateMachinePersist);
		stateMachine.getStateMachineAccessor()
			.doWithRegion(new StateMachineFunction<StateMachineAccess<String, String>>(){
			@Override
			public void apply(StateMachineAccess<String, String> function) {
				function.addStateMachineInterceptor(interceptor);
			}
		});
		listener.resetMessages();
		return restoreStateMachine(stateMachine, jpaRepositoryStateMachinePersist.read(machineId));
	}

	private StateMachine<String, String> restoreStateMachine(StateMachine<String, String> stateMachine,
			StateMachineContext<String, String> stateMachineContext) {
		if (stateMachineContext == null) {
			return stateMachine;
		}
		stateMachine.stop();
		stateMachine.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<String, String>>() {

			@Override
			public void apply(StateMachineAccess<String, String> function) {
				function.resetStateMachine(stateMachineContext);
			}
		});
		return stateMachine;
	}

	private String[] getEvents() {
		List<String> events = new ArrayList<>();
		for (RepositoryTransition t : transitionRepository.findAll()) {
			events.add(t.getEvent());
		}
		return events.toArray(new String[0]);
	}

	private String createMessages(List<String> messages) {
		StringBuilder buf = new StringBuilder();
		for (String message : messages) {
			buf.append(message);
			buf.append("\n");
		}
		return buf.toString();
	}
}
