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
