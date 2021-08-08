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

package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests parallel invocations.
 * 
 * @author Daniel Sagenschneider
 */
public class ParallelThenSequentialTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure invokes parallel task with any {@link Team}.
	 */
	public void testParallelWithAnyTeam() throws Exception {
		this.doTest((Team) null);
	}

	/**
	 * Ensures invokes parallel task with passive {@link Team}.
	 */
	public void testParallelWithPassiveTeam() throws Exception {
		this.doTest(PassiveTeamSource.createPassiveTeam());
	}

	/**
	 * Ensures invokes parallel task with active {@link Team}.
	 */
	public void testParallelWithActiveTeam() throws Exception {
		this.doTest(OnePersonTeamSource.createOnePersonTeam("PARALLEL"));
	}

	/**
	 * <p>
	 * Does the parallel test with the input {@link Team}.
	 * <p>
	 * Given any type of team, the order of {@link FunctionState} execution
	 * should be the same.
	 * 
	 * @param team
	 *            {@link Team}.
	 */
	public void doTest(Team team) throws Exception {

		// Configure
		if (team != null) {
			this.constructTeam("team", team);
		}
		ParallelWorkObject work = new ParallelWorkObject();
		ReflectiveFunctionBuilder firstFunctionBuilder = this.constructFunction(work, "first");
		if (team != null) {
			firstFunctionBuilder.getBuilder().setResponsibleTeam("team");
		}
		firstFunctionBuilder.buildFlow("parallel", Object.class, false);
		firstFunctionBuilder.setNextFunction("second");
		ReflectiveFunctionBuilder parallel = this.constructFunction(work, "parallel");
		if (team != null) {
			parallel.getBuilder().setResponsibleTeam("team");
		}
		ReflectiveFunctionBuilder second = this.constructFunction(work, "second");
		if (team != null) {
			second.getBuilder().setResponsibleTeam("team");
		}

		// Run
		this.invokeFunctionAndValidate("first", null, "first", "parallel", "second");
	}

	public static class ParallelWorkObject {

		public void first(ReflectiveFlow flow) {
			flow.doFlow(null, new FlowCallback() {
				@Override
				public void run(Throwable escalation) throws Throwable {
				}
			});
		}

		public void parallel() {
		}

		public void second() {
		}
	}

}
