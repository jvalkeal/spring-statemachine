/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demo.cdplayer;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;

import demo.CommonConfiguration;
import demo.cdplayer.Application.Events;
import demo.cdplayer.Application.States;

public class CdPlayerTests {

	private final static Log log = LogFactory.getLog(CdPlayerTests.class);

	private AnnotationConfigApplicationContext context;

	private StateMachine<States,Events> machine;

	private CdPlayer player;

	private Library library;

	private TestListener listener;

//	@Test
	public void testInitialState() throws InterruptedException {
		System.out.println("testInitialState");
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(2));
		assertThat(machine.getState().getIds(), contains(States.IDLE, States.CLOSED));
		assertLcdStatusStartsWith("No CD");
	}

//	@Test
	public void testEjectTwice() throws Exception {
		System.out.println("testEjectTwice");
		listener.reset(1, 0, 0);
		player.eject();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), contains(States.IDLE, States.OPEN));
		listener.reset(1, 0, 0);
		player.eject();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), contains(States.IDLE, States.CLOSED));
	}

//	@Test
	public void testPlayWithCdLoaded() throws Exception {
		System.out.println("testPlayWithCdLoaded");
		listener.reset(4, 0, 0);
		player.eject();
		player.load(library.getCollection().get(0));
		player.eject();
		player.play();
		assertThat(listener.stateChangedLatch.await(5, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(4));
		assertThat(machine.getState().getIds(), contains(States.BUSY, States.PLAYING));
		assertLcdStatusContains("cd1");
	}

//	@Test
	public void testPlayWithCdLoadedDeckOpen() throws Exception {
		System.out.println("testPlayWithCdLoadedDeckOpen");
		listener.reset(3, 0, 0);
		player.eject();
		player.load(library.getCollection().get(0));
		player.play();
		assertThat(listener.stateChangedLatch.await(5, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(4));
		assertThat(machine.getState().getIds(), contains(States.BUSY, States.PLAYING));
		assertLcdStatusContains("cd1");
	}

//	@Test
	public void testPlayWithNoCdLoaded() throws Exception {
		System.out.println("testPlayWithNoCdLoaded");
		listener.reset(0, 0, 0);
		player.play();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(0));
		assertThat(machine.getState().getIds(), contains(States.IDLE, States.CLOSED));
		assertLcdStatusStartsWith("No CD");
	}

//	@Test
	public void testPlayLcdTimeChanges() throws Exception {
		System.out.println("testPlayLcdTimeChanges");
		listener.reset(4, 0, 0);
		player.eject();
		player.load(library.getCollection().get(0));
		player.eject();
		player.play();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(4));
		assertThat(machine.getState().getIds(), contains(States.BUSY, States.PLAYING));
		assertLcdStatusContains("cd1");

		listener.reset(0, 0, 0, 1);
		assertThat(listener.transitionLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.transitionCount, is(1));
		assertLcdStatusContains("00:0");
		String ldcStatus1 = player.getLdcStatus();

		listener.reset(0, 0, 0, 1);
		assertThat(listener.transitionLatch.await(2, TimeUnit.SECONDS), is(true));
		assertLcdStatusContains("00:0");
		assertThat(listener.transitionCount, is(1));
		String ldcStatus2 = player.getLdcStatus();
		assertThat(ldcStatus1, not(is(ldcStatus2)));

		listener.reset(0, 0, 0, 2);
		assertThat(listener.transitionLatch.await(4, TimeUnit.SECONDS), is(true));
		assertThat(listener.transitionCount, is(2));
		// ok we have some timing problems with
		// this test, so for now just check it's
		// not previous
		assertLcdStatusNotContains("00:02");
	}

//	@Test
	public void testPlayPause() throws Exception {
		System.out.println("testPlayPause");
		listener.reset(4, 0, 0);
		player.eject();
		player.load(library.getCollection().get(0));
		player.eject();
		player.play();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(4));
		assertThat(machine.getState().getIds(), contains(States.BUSY, States.PLAYING));
		assertLcdStatusContains("cd1");

		listener.reset(0, 0, 0, 1);
		assertThat(listener.transitionLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.transitionCount, is(1));
		String ldcStatus1 = player.getLdcStatus();
		assertLcdStatusContains("00:0");

		listener.reset(0, 0, 0, 1);
		assertThat(listener.transitionLatch.await(2, TimeUnit.SECONDS), is(true));
		assertLcdStatusContains("00:0");
		assertThat(listener.transitionCount, is(1));
		String ldcStatus2 = player.getLdcStatus();
		assertThat(ldcStatus1, not(is(ldcStatus2)));


		listener.reset(1, 0, 0, 0);
		player.pause();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		String ldcStatus3 = player.getLdcStatus();
		assertThat(ldcStatus2, is(ldcStatus3));

		listener.reset(1, 0, 0, 1);
		player.pause();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		listener.transitionLatch.await(2, TimeUnit.SECONDS);
		assertThat(listener.stateChangedCount, is(1));
		assertThat(listener.transitionCount, is(1));

		listener.reset(0, 0, 0, 2);
		assertThat(listener.transitionLatch.await(2100, TimeUnit.MILLISECONDS), is(true));
		assertThat(listener.transitionCount, is(2));
		assertLcdStatusNotContains("00:02");
	}

	@Test
	public void testPlayStop() throws Exception {
		log.info("testPlayStop");
		listener.reset(1, 0, 0);
		log.info("testPlayStop EJECT");
		player.eject();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));

		listener.reset(0, 0, 0, 0, 1);
		log.info("testPlayStop LOAD");
		player.load(library.getCollection().get(0));
		assertThat(listener.loadTransitionLatch.await(2, TimeUnit.SECONDS), is(true));

		listener.reset(1, 0, 0);
		log.info("testPlayStop EJECT");
		player.eject();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));

		listener.reset(1, 0, 0);
		log.info("testPlayStop PLAY");
		player.play();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));

//		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
//		assertThat(listener.stateChangedCount, is(4));
		assertThat(machine.getState().getIds(), contains(States.BUSY, States.PLAYING));

		listener.reset(2, 0, 0);
		player.stop();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(2));
		assertLcdStatusIs("cd1 ");
	}

	@Test
	public void testPlayStop2() throws Exception {
		log.info("testPlayStop2");
		listener.reset(1, 0, 0);
		log.info("testPlayStop2 EJECT");
		player.eject();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));

		listener.reset(0, 0, 0, 0, 1);
		log.info("testPlayStop2 LOAD");
		player.load(library.getCollection().get(0));
		assertThat(listener.loadTransitionLatch.await(2, TimeUnit.SECONDS), is(true));

		listener.reset(1, 0, 0);
		log.info("testPlayStop2 EJECT");
		player.eject();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));

		listener.reset(1, 0, 0);
		log.info("testPlayStop2 PLAY");
		player.play();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));

//		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
//		assertThat(listener.stateChangedCount, is(4));
		assertThat(machine.getState().getIds(), contains(States.BUSY, States.PLAYING));

		listener.reset(2, 0, 0);
		player.stop();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(2));
		assertLcdStatusIs("cd1 ");
	}

//	@Test
	public void testPlayStop3() throws Exception {
		log.info("testPlayStop2");
		listener.reset(4, 0, 0);
		log.info("testPlayStop2 EJECT");
		player.eject();
		log.info("testPlayStop2 LOAD");
		player.load(library.getCollection().get(0));
		log.info("testPlayStop2 EJECT");
		player.eject();
		log.info("testPlayStop2 PLAY");
		player.play();

		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(4));
		assertThat(machine.getState().getIds(), contains(States.BUSY, States.PLAYING));

		listener.reset(2, 0, 0);
		player.stop();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(2));
		assertLcdStatusIs("cd1 ");
	}

//	@Test
	public void testPlayDeckOpenNoCd() throws Exception {
		System.out.println("testPlayDeckOpenNoCd");
		listener.reset(2, 0, 0);
		player.eject();
		player.play();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(2));
		assertThat(machine.getState().getIds(), contains(States.IDLE, States.CLOSED));
	}

	private void assertLcdStatusIs(String text) {
		assertThat(player.getLdcStatus(), is(text));
	}

	private void assertLcdStatusStartsWith(String text) {
		assertThat(player.getLdcStatus(), startsWith(text));
	}

	private void assertLcdStatusContains(String text) {
		assertThat(player.getLdcStatus(), containsString(text));
	}

	private void assertLcdStatusNotContains(String text) {
		assertThat(player.getLdcStatus(), not(containsString(text)));
	}

	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws Exception {
		System.out.println("setup 1");
		context = new AnnotationConfigApplicationContext();
		context.register(CommonConfiguration.class, Application.class, TestConfig.class);
		context.refresh();
		machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		player = context.getBean(CdPlayer.class);
		library = context.getBean(Library.class);
		listener = context.getBean(TestListener.class);
		machine.start();
		// lets do a little sleep to wait sm to start
		Thread.sleep(1000);
		System.out.println("setup 2");
	}

	@After
	public void clean() throws Exception {
		System.out.println("clean 1");
		machine.stop();
		context.close();
		context = null;
		machine = null;
		player = null;
		library = null;
		listener = null;
//		Thread.sleep(1000);
		System.out.println("clean 2");
	}

	static class TestConfig {

		@Autowired
		private StateMachine<States,Events> machine;

		@Bean
		public StateMachineListener<States, Events> stateMachineListener() {
			TestListener listener = new TestListener();
			machine.addStateListener(listener);
			return listener;
		}

		@Bean
		public Library library() {
			// override library to make it easier to test
			Track cd1track1 = new Track("cd1track1", 30);
			Track cd1track2 = new Track("cd1track2", 30);
			Cd cd1 = new Cd("cd1", new Track[]{cd1track1,cd1track2});
			Track cd2track1 = new Track("cd2track1", 30);
			Track cd2track2 = new Track("cd2track2", 30);
			Cd cd2 = new Cd("cd2", new Track[]{cd2track1,cd2track2});
			return new Library(new Cd[]{cd1,cd2});
		}

	}

	static class TestListener extends StateMachineListenerAdapter<States, Events> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile CountDownLatch stateEnteredLatch = new CountDownLatch(2);
		volatile CountDownLatch stateExitedLatch = new CountDownLatch(0);
		volatile CountDownLatch transitionLatch = new CountDownLatch(0);
		volatile CountDownLatch loadTransitionLatch = new CountDownLatch(0);
		volatile int stateChangedCount = 0;
		volatile int transitionCount = 0;
		List<State<States, Events>> statesEntered = new ArrayList<State<States,Events>>();
		List<State<States, Events>> statesExited = new ArrayList<State<States,Events>>();

		@Override
		public void stateChanged(State<States, Events> from, State<States, Events> to) {
			stateChangedCount++;
			stateChangedLatch.countDown();
		}

		@Override
		public void stateEntered(State<States, Events> state) {
			statesEntered.add(state);
			stateEnteredLatch.countDown();
		}

		@Override
		public void stateExited(State<States, Events> state) {
			statesExited.add(state);
			stateExitedLatch.countDown();
		}

		@Override
		public void transitionEnded(Transition<States, Events> transition) {
			transitionCount++;
			if (transition.getKind() == TransitionKind.INTERNAL && transition.getSource().getId() == States.OPEN) {
				loadTransitionLatch.countDown();
			}
			transitionLatch.countDown();
		}

		public void reset(int c1, int c2, int c3) {
			reset(c1, c2, c3, 0);
		}

		public void reset(int c1, int c2, int c3, int c4) {
			reset(c1, c2, c3, c4, 0);
		}

		public void reset(int c1, int c2, int c3, int c4, int c5) {
			stateChangedLatch = new CountDownLatch(c1);
			stateEnteredLatch = new CountDownLatch(c2);
			stateExitedLatch = new CountDownLatch(c3);
			transitionLatch = new CountDownLatch(c4);
			loadTransitionLatch = new CountDownLatch(c5);
			stateChangedCount = 0;
			transitionCount = 0;
			statesEntered.clear();
			statesExited.clear();
		}

	}

}
