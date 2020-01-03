/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.stress.object;

import junit.framework.TestSuite;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests the {@link ContextAwareManagedObject} name.
 * 
 * @author Daniel Sagenschneider
 */
public class NameContextAwareObjectStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(NameContextAwareObjectStressTest.class);
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
			assertEquals("Incorrect bound name", this.expectedBoundName, nameAware.context.getBoundName());
			assertEquals("Incorrect logger", this.expectedBoundName, nameAware.context.getLogger().getName());

			// Determine if complete
			if (context.incrementIterationAndIsComplete()) {
				return;
			}

			// Undertake another
			flow.doFlow(null, null);
		}
	}

	/**
	 * {@link ContextAwareManagedObject} for the stress test.
	 */
	private static class StressNameAwareManagedObject implements ContextAwareManagedObject {

		private ManagedObjectContext context = null;

		@Override
		public void setManagedObjectContext(ManagedObjectContext context) {
			this.context = context;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}
