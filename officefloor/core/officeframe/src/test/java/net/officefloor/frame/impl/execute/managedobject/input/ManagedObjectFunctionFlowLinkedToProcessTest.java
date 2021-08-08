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
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService;
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
	 * Ensure {@link ManagedFunction} added by {@link ManagedObjectSource} can have
	 * its {@link Flow} configured to {@link ProcessState} {@link Flow} for the
	 * {@link ManagedObjectSource}.
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
		assertEquals("Incorrect input function", Integer.valueOf(10), InputFunction.parameter);
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

		private ManagedObjectServiceContext<Indexed> serviceContext;

		public InputManagedObjectSource(InputFunction function) {
			this.function = function;
		}

		public void inputManagedObject() {
			this.serviceContext.invokeProcess(0, 10, this, 0, null);
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
			this.serviceContext = new SafeManagedObjectService<>(context);
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
		public void execute(ManagedFunctionContext<None, Indexed> context) throws Throwable {
			parameter = (Integer) context.getObject(0);
			context.doFlow(0, "TEST", null);
		}
	}

}
