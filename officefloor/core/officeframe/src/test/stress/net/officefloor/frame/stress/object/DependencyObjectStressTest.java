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
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests the {@link CoordinateManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class DependencyObjectStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(DependencyObjectStressTest.class);
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

		// Register the objects
		this.constructManagedObject("COORDINATE", (metaData) -> metaData.addDependency(DependencyObject.class),
				() -> new CoordinateManagedObject());
		this.constructObject("DEPENDENCY", () -> new DependencyObject());

		// Register the name aware task
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder function = this.constructFunction(work, "task");
		context.loadResponsibleTeam(function.getBuilder());
		function.buildObject("COORDINATE", context.getManagedObjectScope()).mapDependency(0, "DEPENDENCY");
		function.buildFlow("task", Integer.class, false);
		this.bindManagedObject("DEPENDENCY", context.getManagedObjectScope(), function.getBuilder());

		// Run the repeats
		context.setInitialFunction("task", 1);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final StressContext context;

		public TestWork(StressContext context) {
			this.context = context;
		}

		public void task(CoordinateManagedObject coordinate, ReflectiveFlow flow) {

			// Ensure have dependency
			assertNotNull("Must have dependency", coordinate.dependency);

			// Determine if complete
			if (context.incrementIterationAndIsComplete()) {
				return;
			}

			// Undertake another
			flow.doFlow(null, null);
		}
	}

	/**
	 * {@link CoordinatingManagedObject} for the stress test.
	 */
	private static class CoordinateManagedObject implements CoordinatingManagedObject<Indexed> {

		private DependencyObject dependency;

		@Override
		public void loadObjects(ObjectRegistry<Indexed> registry) throws Throwable {
			this.dependency = (DependencyObject) registry.getObject(0);
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	/**
	 * Dependency object.
	 */
	private static class DependencyObject {
	}

}
