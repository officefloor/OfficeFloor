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

package net.officefloor.frame.impl.execute.managedobject.coordinate;

import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
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
