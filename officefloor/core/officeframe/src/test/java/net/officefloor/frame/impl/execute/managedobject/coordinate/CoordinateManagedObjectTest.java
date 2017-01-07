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
package net.officefloor.frame.impl.execute.managedobject.coordinate;

import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests coordinating of {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class CoordinateManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * {@link CoordinatingMo} from the {@link CoordinatingWork}.
	 */
	private CoordinatingMo coordinatingMo;

	/**
	 * Ensures able to coordinate {@link ManagedObject} with all
	 * {@link ManagedObjectScope} scopes.
	 */
	public void testEnsureCoordinateAllScopes() throws Throwable {

		String workOne = "WORK_ONE";
		String workTwo = "WORK_TWO";
		String threadOne = "THREAD_ONE";
		String threadTwo = "THREAD_TWO";
		String processOne = "PROCESS_ONE";
		String processTwo = "PROCESS_TWO";

		// Obtain the office name and builder
		String officeName = this.getOfficeName();
		OfficeBuilder officeBuilder = this.getOfficeBuilder();

		// Construct the coordinating managed object
		this.constructManagedObject("coordinate", CoordinatingManagedObjectSource.class, officeName);

		// Construct the managed objects
		// (Constructed after coordinating to ensure dependency ordering)
		this.constructManagedObject(workOne, "workOne", officeName);
		this.constructManagedObject(workTwo, "workTwo", officeName);
		this.constructManagedObject(threadOne, "threadOne", officeName);
		officeBuilder.addThreadManagedObject("threadOne", "threadOne");
		this.constructManagedObject(threadTwo, "threadTwo", officeName);
		officeBuilder.addThreadManagedObject("threadTwo", "threadTwo");
		this.constructManagedObject(processOne, "processOne", officeName);
		officeBuilder.addProcessManagedObject("processOne", "processOne");
		this.constructManagedObject(processTwo, "processTwo", officeName);
		officeBuilder.addProcessManagedObject("processTwo", "processTwo");

		// Construct function to obtain the coordinating managed object
		ReflectiveFunctionBuilder functionBuilder = this.constructFunction(new CoordinatingWork(), "task");
		functionBuilder.getBuilder().addManagedObject("workOne", "workOne");
		functionBuilder.getBuilder().addManagedObject("workTwo", "workTwo");

		// Construct the function to obtain the coordinating mo
		DependencyMappingBuilder mapper = functionBuilder.buildObject("coordinate", ManagedObjectScope.FUNCTION);
		mapper.mapDependency(CoordinatingDependencyKey.WORK_ONE, "workOne");
		mapper.mapDependency(CoordinatingDependencyKey.WORK_TWO, "workTwo");
		mapper.mapDependency(CoordinatingDependencyKey.THREAD_ONE, "threadOne");
		mapper.mapDependency(CoordinatingDependencyKey.THREAD_TWO, "threadTwo");
		mapper.mapDependency(CoordinatingDependencyKey.PROCESS_ONE, "processOne");
		mapper.mapDependency(CoordinatingDependencyKey.PROCESS_TWO, "processTwo");

		// Invoke the function to obtain the coordinating mo
		this.invokeFunction("task", null);

		// Ensure no top level issue
		this.validateNoTopLevelEscalation();

		// Validate the dependencies
		assertNotNull("Must obtain coordinating mo", this.coordinatingMo);
		assertEquals("Incorrect workOne", workOne, this.coordinatingMo.workOne);
		assertEquals("Incorrect workTwo", workTwo, this.coordinatingMo.workTwo);
		assertEquals("Incorrect threadOne", threadOne, this.coordinatingMo.threadOne);
		assertEquals("Incorrect threadTwo", threadTwo, this.coordinatingMo.threadTwo);
		assertEquals("Incorrect processOne", processOne, this.coordinatingMo.processOne);
		assertEquals("Incorrect processTwo", processTwo, this.coordinatingMo.processTwo);
	}

	/**
	 * Coordinating {@link ManagedObjectSource}.
	 */
	@TestSource
	public static class CoordinatingManagedObjectSource
			extends AbstractManagedObjectSource<CoordinatingDependencyKey, None> {

		/*
		 * ============= AbstractAsyncManagedObjectSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<CoordinatingDependencyKey, None> context) throws Exception {
			// Loads the meta-data
			context.setManagedObjectClass(CoordinatingManagedObject.class);
			context.setObjectClass(CoordinatingMo.class);
			context.addDependency(CoordinatingDependencyKey.WORK_ONE, String.class);
			context.addDependency(CoordinatingDependencyKey.WORK_TWO, String.class);
			context.addDependency(CoordinatingDependencyKey.THREAD_ONE, String.class);
			context.addDependency(CoordinatingDependencyKey.THREAD_TWO, String.class);
			context.addDependency(CoordinatingDependencyKey.PROCESS_ONE, String.class);
			context.addDependency(CoordinatingDependencyKey.PROCESS_TWO, String.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return new CoordinatingMo();
		}
	}

	/**
	 * Dependency keys for the coordinating {@link ManagedObject}.
	 */
	public static enum CoordinatingDependencyKey {
		WORK_ONE, WORK_TWO, THREAD_ONE, THREAD_TWO, PROCESS_ONE, PROCESS_TWO
	}

	/**
	 * {@link CoordinatingManagedObject}.
	 */
	private static class CoordinatingMo implements CoordinatingManagedObject<CoordinatingDependencyKey> {

		/**
		 * {@link ManagedObjectScope#FUNCTION} dependency one.
		 */
		public Object workOne;

		/**
		 * {@link ManagedObjectScope#FUNCTION} dependency two.
		 */
		public Object workTwo;

		/**
		 * {@link ManagedObjectScope#THREAD} dependency one.
		 */
		public Object threadOne;

		/**
		 * {@link ManagedObjectScope#THREAD} dependency two.
		 */
		public Object threadTwo;

		/**
		 * {@link ManagedObjectScope#PROCESS} dependency one.
		 */
		public Object processOne;

		/**
		 * {@link ManagedObjectScope#PROCESS} dependency two.
		 */
		public Object processTwo;

		@Override
		public void loadObjects(ObjectRegistry<CoordinatingDependencyKey> registry) throws Throwable {
			// Obtain the dependencies
			this.workOne = registry.getObject(CoordinatingDependencyKey.WORK_ONE);
			this.workTwo = registry.getObject(CoordinatingDependencyKey.WORK_TWO);
			this.threadOne = registry.getObject(CoordinatingDependencyKey.THREAD_ONE);
			this.threadTwo = registry.getObject(CoordinatingDependencyKey.THREAD_TWO);
			this.processOne = registry.getObject(CoordinatingDependencyKey.PROCESS_ONE);
			this.processTwo = registry.getObject(CoordinatingDependencyKey.PROCESS_TWO);
		}

		@Override
		public Object getObject() throws Exception {
			return this;
		}
	}

	/**
	 * Coordinating {@link ManagedFunction} to test coordination.
	 */
	public class CoordinatingWork {

		/**
		 * Task to obtain the {@link CoordinatingMo}.
		 *
		 * @param mo
		 *            {@link CoordinatingMo}.
		 */
		public void task(CoordinatingMo mo) {
			CoordinateManagedObjectTest.this.coordinatingMo = mo;
		}
	}

}