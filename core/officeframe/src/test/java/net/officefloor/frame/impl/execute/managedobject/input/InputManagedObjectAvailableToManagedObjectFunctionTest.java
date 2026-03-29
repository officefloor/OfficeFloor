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
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Ensures the input {@link ManagedObject} is available to the
 * {@link ManagedFunction} instances invoked from the
 * {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class InputManagedObjectAvailableToManagedObjectFunctionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure input {@link ManagedObject} is available to the
	 * {@link ManagedFunction} instances invoked from the
	 * {@link ManagedObjectSource}.
	 */
	public void testEnsureInputAvailbleToFunction() throws Exception {

		// Construct the OfficeFloor
		InputFunction function = new InputFunction();
		InputManagedObjectSource mos = new InputManagedObjectSource(function);
		ManagedObjectBuilder<Indexed> mo = this.constructManagedObject("INPUT", mos, null);
		mo.setManagingOffice(this.getOfficeName()).setInputManagedObjectName("INPUT");
		OfficeFloor officeFloor = this.constructOfficeFloor();

		// Input the managed object
		officeFloor.openOfficeFloor();
		mos.inputManagedObject();

		// Ensure obtain access to input managed object
		assertSame("Should have access to input managed object", mos, function.managedObject);
	}

	public class InputManagedObjectSource extends AbstractManagedObjectSource<None, Indexed> implements ManagedObject {

		private final InputFunction function;

		private ManagedObjectServiceContext<Indexed> serviceContext;

		public InputManagedObjectSource(InputFunction function) {
			this.function = function;
		}

		public void inputManagedObject() {
			this.serviceContext.invokeProcess(0, this, this, 0, null);
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
			context.addFlow(null);
			ManagedObjectSourceContext<Indexed> mosContext = context.getManagedObjectSourceContext();
			mosContext.addManagedFunction("function", this.function).linkManagedObject(0);
			mosContext.getFlow(0).linkFunction("function");
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

	public class InputFunction extends StaticManagedFunction<Indexed, None> {

		private InputManagedObjectSource managedObject = null;

		/*
		 * ===================== ManagedFunction ==============================
		 */

		@Override
		public void execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {
			this.managedObject = (InputManagedObjectSource) context.getObject(0);
		}
	}

}
