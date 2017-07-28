/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Ensure {@link ManagedFunction} added by {@link ManagedObjectSource} can have
 * its {@link Flow} configured to {@link ProcessState} {@link Flow} for the
 * {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectFunctionFlowLinkedToProcessTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link ManagedFunction} added by {@link ManagedObjectSource} can
	 * have its {@link Flow} configured to {@link ProcessState} {@link Flow} for
	 * the {@link ManagedObjectSource}.
	 */
	public void testManagedObjectFunctionFlowLinkedToProcess() throws Exception {

		// Rest for test
		InputFunction.parameter = null;
		MockFunction.value = null;

		// Construct the OfficeFloor
		InputFunction function = new InputFunction();
		InputManagedObjectSource mos = new InputManagedObjectSource(function);
		ManagedObjectBuilder<Indexed> mo = this.constructManagedObject("INPUT", mos, null);
		ManagingOfficeBuilder<Indexed> managingOffice = mo.setManagingOffice(this.getOfficeName());
		managingOffice.setInputManagedObjectName("INPUT");
		managingOffice.linkFlow(1, "function");
		this.constructFunction(new MockFunction(), "function").buildParameter();
		OfficeFloor officeFloor = this.constructOfficeFloor();

		// Input the managed object
		officeFloor.openOfficeFloor();
		mos.inputManagedObject();

		// Ensure functions are invoked with correct parameters
		assertEquals("Incorrect input function", new Integer(10), InputFunction.parameter);
		assertEquals("Incorrect function", "TEST", MockFunction.value);
	}

	public static class MockFunction {

		private static String value;

		public void function(String parameter) {
			value = parameter;
		}
	}

	public class InputManagedObjectSource extends AbstractManagedObjectSource<None, Indexed> implements ManagedObject {

		private final InputFunction function;

		private ManagedObjectExecuteContext<Indexed> executeContext;

		public InputManagedObjectSource(InputFunction function) {
			this.function = function;
		}

		public void inputManagedObject() {
			this.executeContext.invokeProcess(0, 10, this, 0, null);
		}

		/*
		 * ===================== ManagedObjectSource ==========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Indexed> context) throws Exception {
			context.setObjectClass(this.getClass());
			context.addFlow(Integer.class);
			context.addFlow(String.class);
			ManagedObjectSourceContext<Indexed> mosContext = context.getManagedObjectSourceContext();
			mosContext.getFlow(0).linkFunction("inputFunction");
			ManagedObjectFunctionBuilder<None, Indexed> function = mosContext.addManagedFunction("inputFunction",
					this.function);
			function.linkParameter(0, Integer.class);
			function.linkFlow(0, mosContext.getFlow(1), String.class, false);
		}

		@Override
		public void start(ManagedObjectExecuteContext<Indexed> context) throws Exception {
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

	public static class InputFunction extends StaticManagedFunction<None, Indexed> {

		private static Integer parameter;

		/*
		 * ===================== ManagedFunction ==============================
		 */

		@Override
		public Object execute(ManagedFunctionContext<None, Indexed> context) throws Throwable {
			parameter = (Integer) context.getObject(0);
			context.doFlow(0, "TEST", null);
			return null;
		}
	}

}