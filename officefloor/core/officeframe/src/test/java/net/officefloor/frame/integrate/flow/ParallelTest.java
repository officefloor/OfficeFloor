/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.integrate.flow;

import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Tests parallel invocations.
 * 
 * @author Daniel Sagenschneider
 */
public class ParallelTest extends AbstractOfficeConstructTestCase {

	/**
	 * Flag to record task method invocations.
	 */
	public ParallelTest() {
		this.setRecordReflectiveTaskMethodsInvoked(true);
	}

	/**
	 * Ensures invokes parallel task with parallel team.
	 */
	public void testParallelWithPassiveTeam() throws Exception {
		this.doTest(new PassiveTeam());
	}

	/**
	 * Ensures invokes parallel task with active team.
	 */
	public void testParallelWithActiveTeam() throws Exception {
		this.doTest(new OnePersonTeam("PARALLEL", MockTeamSource
				.createTeamIdentifier(), 100));
	}

	/**
	 * <p>
	 * Does the parallel test with the input {@link Team}.
	 * <p>
	 * Given any type of team, the order of {@link JobNode} execution should be
	 * the same.
	 * 
	 * @param team
	 *            {@link Team}.
	 */
	public void doTest(Team team) throws Exception {

		// Configure
		this.constructTeam("team", team);
		ReflectiveWorkBuilder workBuilder = this.constructWork(
				new ParallelWorkObject("TestParameter"), "work", "first");
		ReflectiveTaskBuilder firstTaskBuilder = workBuilder.buildTask("first",
				"team");
		firstTaskBuilder.buildFlow("parallel",
				FlowInstigationStrategyEnum.PARALLEL, Object.class);
		firstTaskBuilder.setNextTaskInFlow("second");
		ReflectiveTaskBuilder parallelTaskBuilder = workBuilder.buildTask(
				"parallel", "team");
		parallelTaskBuilder.buildParameter();
		workBuilder.buildTask("second", "team");

		// Run
		this.invokeWork("work", null);

		// Validate methods invoked
		this.validateReflectiveMethodOrder("first", "parallel", "second");
	}

	public static class ParallelWorkObject {

		public final Object inputParameter;

		public volatile Object parallelParameter;

		public ParallelWorkObject(Object inputParameter) {
			this.inputParameter = inputParameter;
		}

		public void first(ReflectiveFlow flow) {
			flow.doFlow(this.inputParameter);
		}

		public void parallel(Object parameter) {
			this.parallelParameter = parameter;
		}

		public void second() {
		}
	}

}