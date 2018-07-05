/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.stress.object;

import junit.framework.TestSuite;
import net.officefloor.frame.api.managedobject.NameAwareManagedObject;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests the {@link NameAwareManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class NameAwareObjectStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(NameAwareObjectStressTest.class);
	}

	@Override
	protected int getIterationCount() {
		return 10000000;
	}

	@Override
	protected boolean isTestEachManagedObjectScope() {
		return true;
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		final String BOUND_NAME = "BOUND_NAME";

		// Register the name aware managed object
		this.constructManagedObject(BOUND_NAME, null, () -> new StressNameAwareManagedObject());

		// Register the name aware task
		TestWork work = new TestWork(context, BOUND_NAME);
		ReflectiveFunctionBuilder function = this.constructFunction(work, "task");
		context.loadResponsibleTeam(function.getBuilder());
		function.buildObject(BOUND_NAME, context.getManagedObjectScope());
		function.buildFlow("task", Integer.class, false);

		// Run the repeats
		context.setInitialFunction("task", 1);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final StressContext context;

		private final String expectedBoundName;

		public TestWork(StressContext context, String expectedBoundName) {
			this.context = context;
			this.expectedBoundName = expectedBoundName;
		}

		public void task(StressNameAwareManagedObject nameAware, ReflectiveFlow flow) {

			// Ensure the bound name is correct
			assertEquals("Incorrect bound name", this.expectedBoundName, nameAware.boundName);

			// Determine if complete
			if (context.incrementIterationAndIsComplete()) {
				return;
			}

			// Undertake another
			flow.doFlow(null, null);
		}
	}

	/**
	 * {@link NameAwareManagedObject} for the stress test.
	 */
	private static class StressNameAwareManagedObject implements NameAwareManagedObject {

		private String boundName = null;

		@Override
		public void setBoundManagedObjectName(String boundManagedObjectName) {
			this.boundName = boundManagedObjectName;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}