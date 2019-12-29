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
package net.officefloor.frame.impl.execute.managedobject.input;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Ensure {@link ManagedFunction} added by {@link ManagedObjectSource} can have
 * a dependency {@link ManagedObject} linked.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectFunctionDependencyTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link ManagedFunction} added by {@link ManagedObjectSource} can have
	 * a dependency {@link ManagedObject} linked.
	 */
	public void testManagedObjectFunctionDependency() throws Exception {

		// Construct the managed object with function
		InputFunction function = new InputFunction();
		InputManagedObjectSource mos = new InputManagedObjectSource(function);
		ManagedObjectBuilder<None> mo = this.constructManagedObject("INPUT", mos, null);
		ManagingOfficeBuilder<None> managingOffice = mo.setManagingOffice(this.getOfficeName());
		managingOffice.mapFunctionDependency("dependency", "DEPENDENCY");
		managingOffice.setInputManagedObjectName("INPUT");

		// Construct the dependency
		MockDependency dependency = new MockDependency();
		this.constructManagedObject(dependency, "DEPENDENCY_MO", this.getOfficeName());
		this.getOfficeBuilder().addThreadManagedObject("DEPENDENCY", "DEPENDENCY_MO");

		// Construct the OfficeFloor
		OfficeFloor officeFloor = this.constructOfficeFloor();

		// Input the managed object
		officeFloor.openOfficeFloor();
		mos.inputManagedObject();

		// Ensure dependency available to function
		assertEquals("Function should have dependency", dependency, function.dependency);
	}

	public static class MockDependency {
	}

	public static class InputFunction extends StaticManagedFunction<None, Indexed> {

		private MockDependency dependency;

		/*
		 * ===================== ManagedFunction ==============================
		 */

		@Override
		public void execute(ManagedFunctionContext<None, Indexed> context) throws Throwable {
			this.dependency = (MockDependency) context.getObject(0);
		}
	}

	public class InputManagedObjectSource extends AbstractManagedObjectSource<None, None> implements ManagedObject {

		private final InputFunction function;

		private ManagedObjectExecuteContext<None> executeContext;

		public InputManagedObjectSource(InputFunction function) {
			this.function = function;
		}

		public void inputManagedObject() {
			this.executeContext.invokeProcess(0, null, this, 0, null);
		}

		/*
		 * ===================== ManagedObjectSource ==========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

			// Load meta-data
			context.setObjectClass(this.getClass());

			// Create the function and link in
			context.addFlow(null);
			mosContext.getFlow(0).linkFunction("inputFunction");
			ManagedObjectFunctionBuilder<None, Indexed> function = mosContext.addManagedFunction("inputFunction",
					this.function);

			// Link function to dependency
			ManagedObjectFunctionDependency dependency = mosContext.addFunctionDependency("dependency",
					MockDependency.class);
			function.linkObject(0, dependency);
		}

		@Override
		public void start(ManagedObjectExecuteContext<None> context) throws Exception {
			this.executeContext = context;
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ======================== ManagedObject =============================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}