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
package net.officefloor.frame.stress.object;

import junit.framework.TestSuite;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.NameAwareManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests the {@link NameAwareManagedObject}.
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
	 * Dependency object. s
	 */
	private static class DependencyObject {
	}

}