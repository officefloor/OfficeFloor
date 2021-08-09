/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
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
 * #L%
 */

package net.officefloor.frame.impl.spi.team;

import static org.junit.Assert.assertNotEquals;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.team.ThreadLocalAwareTeam;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests the {@link ThreadLocalAwareTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalAwareTeamTest extends AbstractOfficeConstructTestCase {

	/**
	 * {@link ThreadLocal}.
	 */
	private final ThreadLocal<List<Integer>> threadLocal = new ThreadLocal<List<Integer>>() {
		@Override
		protected List<Integer> initialValue() {
			return new LinkedList<Integer>();
		}
	};

	/**
	 * Ensure can invoke function with {@link ThreadLocal} awareness.
	 */
	public void testInvokeFunctionsWithThreadLocalAwareness() throws Exception {

		// Obtain the thread names
		OnePersonTeam anotherTeam = OnePersonTeamSource.createOnePersonTeam("ANOTHER");
		String contextThreadName = Thread.currentThread().getName();

		// Construct the teams
		this.constructTeam("CONTEXT", ThreadLocalAwareTeamSource.class);
		this.constructTeam("ANOTHER", anotherTeam);

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder taskOne = this.constructFunction(work, "taskOne");
		taskOne.buildParameter();
		taskOne.getBuilder().setResponsibleTeam("CONTEXT");
		taskOne.setNextFunction("taskTwo");
		ReflectiveFunctionBuilder taskTwo = this.constructFunction(work, "taskTwo");
		taskTwo.buildParameter();
		taskTwo.getBuilder().setResponsibleTeam("ANOTHER");
		taskTwo.setNextFunction("taskThree");
		ReflectiveFunctionBuilder taskThree = this.constructFunction(work, "taskThree");
		taskThree.buildParameter();
		taskThree.getBuilder().setResponsibleTeam("CONTEXT");

		// Trigger the function (should block until complete)
		Closure<Throwable> failure = new Closure<>();
		this.triggerFunction("taskOne", Integer.valueOf(1), (escalation) -> failure.value = escalation);
		assertNull("Should be no failure", failure.value);

		// Ensure test is valid with different names
		assertNotEquals("Should be different names", contextThreadName, anotherTeam.getThreadName());

		// Ensure the methods invoked with correct teams
		assertEquals("Incorrect task one thread", contextThreadName, work.taskOneThreadName);
		assertEquals("Incorrect task two thread", anotherTeam.getThreadName(), work.taskTwoThreadName);
		assertEquals("Incorrect task three thread", contextThreadName, work.taskThreeThreadName);

		// Ensure appropriate thread local values
		List<Integer> values = this.threadLocal.get();
		assertEquals("Incorrect number of thread local values", 2, values.size());
		assertEquals("Incorrect first thread local value", Integer.valueOf(1), values.get(0));
		assertEquals("Incorrect second thread local value", Integer.valueOf(3), values.get(1));
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private String taskOneThreadName;

		private volatile String taskTwoThreadName;

		private String taskThreeThreadName;

		public Integer taskOne(Integer parameter) {
			assertEquals("Incorrect parameter", Integer.valueOf(1), parameter);
			this.taskOneThreadName = Thread.currentThread().getName();
			ThreadLocalAwareTeamTest.this.threadLocal.get().add(parameter);
			return Integer.valueOf(2);
		}

		public Integer taskTwo(Integer parameter) {
			assertEquals("Incorrect parameter", Integer.valueOf(2), parameter);
			this.taskTwoThreadName = Thread.currentThread().getName();
			ThreadLocalAwareTeamTest.this.threadLocal.get().add(parameter);
			return Integer.valueOf(3);
		}

		public void taskThree(Integer parameter) {
			assertEquals("Incorrect parameter", Integer.valueOf(3), parameter);
			this.taskThreeThreadName = Thread.currentThread().getName();
			ThreadLocalAwareTeamTest.this.threadLocal.get().add(parameter);
		}
	}

}
