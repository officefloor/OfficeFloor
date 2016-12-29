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
package net.officefloor.frame.integrate.stress;

import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.NameAwareManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder.ReflectiveFunctionBuilder;

/**
 * Stress tests the {@link NameAwareManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class NameAwareTaskStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * Stress tests with the {@link OnePersonTeam}.
	 */
	@StressTest
	public void test_StressNameAware_OnePersonTeam() throws Exception {
		this.doTest(new OnePersonTeam("TEST", MockTeamSource
				.createTeamIdentifier(), 100));
	}

	/**
	 * Stress tests with the {@link LeaderFollowerTeam}.
	 */
	@StressTest
	public void test_StressNameAware_LeaderFollowerTeam() throws Exception {
		this.doTest(new LeaderFollowerTeam("TEST", MockTeamSource
				.createTeamIdentifier(), 5, 100));
	}

	/**
	 * Stress tests with the {@link ExecutorFixedTeamSource}.
	 */
	@StressTest
	public void test_StressNameAware_ExecutorFixedTeam() throws Exception {
		this.doTest(ExecutorFixedTeamSource.createTeam("TEST",
				MockTeamSource.createTeamIdentifier(), 5));
	}

	/**
	 * Does the name aware stress test with the {@link Team}.
	 * 
	 * @param team
	 *            {@link Team}.
	 */
	public void doTest(Team team) throws Exception {

		int SEQUENTIAL_COUNT = 1000000;
		int MAX_RUN_TIME = 100;
		this.setVerbose(true);

		final String BOUND_NAME = "BOUND_NAME";

		// Initiate
		String officeName = this.getOfficeName();
		this.constructTeam("TEAM", team);

		// Register the name aware managed object
		this.constructManagedObject(BOUND_NAME,
				NameAwareManagedObjectSource.class, officeName);

		// Register the name aware task
		NameAwareManagedObjectTask nameAwareMoTask = new NameAwareManagedObjectTask(
				SEQUENTIAL_COUNT, BOUND_NAME);
		ReflectiveFunctionBuilder task = this.constructWork(nameAwareMoTask,
				"work", "nameAware").buildTask("nameAware", "TEAM");
		task.buildParameter();
		task.buildFlow("nameAware", FlowInstigationStrategyEnum.SEQUENTIAL,
				Integer.class);
		task.buildObject(BOUND_NAME, ManagedObjectScope.WORK);

		// Run the repeats
		this.invokeWork("work", new Integer(1), MAX_RUN_TIME);

		// Ensure is complete
		assertEquals("Did not complete all sequential calls", SEQUENTIAL_COUNT,
				nameAwareMoTask.sequentialCallCount.get());
	}

	/**
	 * {@link Work}.
	 */
	public class NameAwareManagedObjectTask {

		/**
		 * Number of times to make a sequential call.
		 */
		private final int maxSequentialCalls;

		/**
		 * Expected bound name.
		 */
		private final String expectedBoundName;

		/**
		 * Number of sequential calls made.
		 */
		public AtomicInteger sequentialCallCount = new AtomicInteger(0);

		/**
		 * Initiate.
		 * 
		 * @param maxSequentialCalls
		 *            Number of times to make a sequential call.
		 * @param expectedBoundName
		 *            Expected bound name.
		 */
		public NameAwareManagedObjectTask(int maxSequentialCalls,
				String expectedBoundName) {
			this.maxSequentialCalls = maxSequentialCalls;
			this.expectedBoundName = expectedBoundName;
		}

		/**
		 * Task to test the name aware managed object.
		 * 
		 * @param callCount
		 *            Number of sequential calls so far.
		 * @param flow
		 *            {@link ReflectiveFlow} to invoke the sequential
		 *            {@link Flow}.
		 * @param nameAware
		 *            {@link StressNameAwareManagedObject}.
		 */
		public void nameAware(Integer callCount, ReflectiveFlow flow,
				StressNameAwareManagedObject nameAware) {

			// Indicate the number of calls made
			this.sequentialCallCount.incrementAndGet();

			// Ensure the bound name is correct
			assertEquals("Incorrect bound name", this.expectedBoundName,
					nameAware.getBoundName());

			// Output progress
			if ((callCount.intValue() % 1000000) == 0) {
				NameAwareTaskStressTest.this
						.printMessage("Name aware task calls="
								+ callCount.intValue());
			}

			// Determine if enough sequential calls
			if (callCount.intValue() < this.maxSequentialCalls) {
				// Make another sequential call
				flow.doFlow(new Integer(callCount.intValue() + 1));
			}
		}
	}

	/**
	 * {@link NameAwareManagedObject} for the stress test.
	 */
	private static class StressNameAwareManagedObject implements
			NameAwareManagedObject {

		/**
		 * Bound name.
		 */
		private String boundName = null;

		/**
		 * Indicates if this has been used. Ensures new
		 * {@link StressNameAwareManagedObject} is used for each invocation.
		 */
		private volatile boolean isUsed = false;

		/**
		 * Obtains the bound name.
		 * 
		 * @return Bound name.
		 */
		public String getBoundName() {

			// Should only be used once
			assertFalse("Should only be used once", this.isUsed);

			// Now used
			this.isUsed = true;
			return this.boundName;
		}

		/*
		 * =================== NameAwareManagedObject ========================
		 */

		@Override
		public void setBoundManagedObjectName(String boundManagedObjectName) {
			this.boundName = boundManagedObjectName;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	/**
	 * Name aware {@link ManagedObjectSource}.
	 */
	@TestSource
	public static class NameAwareManagedObjectSource extends
			AbstractManagedObjectSource<None, None> {

		/*
		 * ================= ManagedObjectSource ========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context)
				throws Exception {
			// Provide meta-data
			context.setManagedObjectClass(StressNameAwareManagedObject.class);
			context.setObjectClass(StressNameAwareManagedObject.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return new StressNameAwareManagedObject();
		}
	}

}