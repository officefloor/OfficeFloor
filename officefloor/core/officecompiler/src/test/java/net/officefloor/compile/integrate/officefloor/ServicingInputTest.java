/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.integrate.officefloor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Ensure can configure service handling for a {@link DeployedOffice}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServicingInputTest {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * {@link FunctionManager} obtained from {@link DeployedOfficeInput}.
	 */
	private FunctionManager internalFunctionManager;

	/**
	 * {@link ExternalServiceInput} obtained from {@link DeployedOfficeInput}.
	 */
	private ExternalServiceInput<ServiceInputObject, ServiceInputObject> externalServiceInput;

	/**
	 * {@link ExternalManagedObjectSource}.
	 */
	private ExternalManagedObjectSource externalManagdObjectSource;

	/**
	 * Value sent from invoker.
	 */
	private static String value = null;

	@BeforeEach
	protected void setUp() throws Exception {

		final String OFFICE_NAME = "OFFICE";
		final MockOfficeSource officeSource = new MockOfficeSource();

		// Compile the OfficeFloor with extension
		CompileOfficeFloor compile = new CompileOfficeFloor();

		// Add the Office
		compile.officeFloor((context) -> {
			// Add the deployed office (configuring the section)
			context.getOfficeFloorDeployer().addDeployedOffice(OFFICE_NAME, officeSource, null);
		});

		// Extend the existing Office
		compile.officeFloor((context) -> {

			// Obtain the existing office (added above)
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
			DeployedOffice office = deployer.getDeployedOffice(OFFICE_NAME);
			assertNotNull(office, "Should have existing Office");

			// External function manager requires no configuration

			// Internal function manager
			this.internalFunctionManager = office.getDeployedOfficeInput("SECTION", "internalFunctionManager")
					.getFunctionManager();

			// External service input
			this.externalServiceInput = office.getDeployedOfficeInput("SECTION", "externalServiceInput")
					.addExternalServiceInput(ServiceInputObject.class, ServiceInputObject.class,
							(inputManagedObject, escalations) -> fail("Should have no escalations"));

			// Internally invoke from managed object
			OfficeFloorManagedObjectSource internalMos = deployer.addManagedObjectSource("MOS_INTERNAL",
					ClassManagedObjectSource.class.getName());
			internalMos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, MockObject.class.getName());
			deployer.link(internalMos.getManagingOffice(), office);
			deployer.link(internalMos, deployer.addInputManagedObject("INPUT_INTERNAL", MockObject.class.getName()));
			DeployedOfficeInput internalMoInput = office.getDeployedOfficeInput("SECTION",
					"managedObjectInternalThread");
			deployer.link(internalMos.getOfficeFloorManagedObjectFlow("doProcess"), internalMoInput);

			// Externally invoke from managed object
			this.externalManagdObjectSource = new ExternalManagedObjectSource();
			OfficeFloorManagedObjectSource externalMos = deployer.addManagedObjectSource("MOS_EXTERNAL",
					this.externalManagdObjectSource);
			deployer.link(externalMos.getManagingOffice(), office);
			deployer.link(externalMos,
					deployer.addInputManagedObject("INPUT_EXTERNAL", ExternalManagedObjectSource.class.getName()));
			DeployedOfficeInput externalMoInput = office.getDeployedOfficeInput("SECTION",
					"managedObjectExternalThread");
			deployer.link(externalMos.getOfficeFloorManagedObjectFlow("0"), externalMoInput);
		});

		// Open the OfficeFloor
		this.officeFloor = compile.compileAndOpenOfficeFloor();

		// Reset for testing
		value = null;
	}

	@AfterEach
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can invoke via external {@link FunctionManager}.
	 */
	@Test
	public void externalFunctionManager() throws Exception {
		FunctionManager function = this.officeFloor.getOffice("OFFICE")
				.getFunctionManager("SECTION.externalFunctionManager");
		function.invokeProcess("EXTERNAL_FUNCTION_MANAGER", null);
		assertEquals("EXTERNAL_FUNCTION_MANAGER", value, "Incorrect value");
	}

	/**
	 * Ensure can invoke via internal {@link FunctionManager}.
	 */
	@Test
	public void internalFunctionManager() throws Exception {
		this.internalFunctionManager.invokeProcess("INTERNAL_FUNCTION_MANAGER", null);
		assertEquals("INTERNAL_FUNCTION_MANAGER", value, "Incorrect value");
	}

	/**
	 * Ensure can invoke via {@link ExternalServiceInput}.
	 */
	@Test
	public void externalServiceInput() throws Exception {
		ServiceInputObject serviceObject = new ServiceInputObject();
		this.externalServiceInput.service(serviceObject, null);
		assertEquals("EXTERNAL_SERVICE_INPUT", value, "Incorrect value");
		assertEquals("ExternalServiceInput_" + ServiceInputObject.class.getName() + ":EXTERNAL_SERVICE_INPUT",
				serviceObject.value, "Incorrect service input servicing");
	}

	/**
	 * Ensure can invoke internally via {@link ManagedObjectSource}.
	 */
	@Test
	public void managedObjectInternalThread() throws Exception {
		FunctionManager function = this.officeFloor.getOffice("OFFICE").getFunctionManager("SECTION.trigger");
		function.invokeProcess("INTERNAL_MANAGED_OBJECT", null);
		assertEquals("INTERNAL_MANAGED_OBJECT", value, "Incorrect value");
	}

	/**
	 * Ensure can invoke externally via {@link ManagedObjectSource}.
	 */
	@Test
	public void managedObjectExternalThread() throws Exception {
		this.externalManagdObjectSource.context.invokeProcess(0, "EXTERNAL_MANAGED_OBJECT",
				this.externalManagdObjectSource, 0, null);
		assertEquals("EXTERNAL_MANAGED_OBJECT", value, "Incorrect value");
	}

	@TestSource
	public static class MockOfficeSource extends AbstractOfficeSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext context) throws Exception {

			// Auto-wire objects for functions
			officeArchitect.enableAutoWireObjects();

			// Add the section with functions
			officeArchitect.addOfficeSection("SECTION", ClassSectionSource.class.getName(),
					MockSection.class.getName());
		}
	}

	public static class MockSection {

		@Next("service")
		public String externalFunctionManager(@Parameter String parameter) {
			return parameter;
		}

		@Next("service")
		public String internalFunctionManager(@Parameter String parameter) {
			return parameter;
		}

		@Next("service")
		public String externalServiceInput() {
			return "EXTERNAL_SERVICE_INPUT";
		}

		public void trigger(@Parameter String parameter, MockObject object) {
			object.flows.doProcess(parameter);
		}

		@Next("service")
		public String managedObjectInternalThread(@Parameter String parameter) {
			return parameter;
		}

		@Next("service")
		public String managedObjectExternalThread(@Parameter String parameter) {
			return parameter;
		}

		public void service(@Parameter String parameter, ServiceInputObject input) {
			if (input != null) {
				input.value = input.name + ":" + parameter;
			}
			value = parameter;
		}
	}

	private static class ServiceInputObject implements ContextAwareManagedObject {

		private String name;

		private String value = null;

		@Override
		public void setManagedObjectContext(ManagedObjectContext context) {
			this.name = context.getBoundName();
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	@FlowInterface
	public static interface MockProcessFlows {
		void doProcess(String parameter);
	}

	public static class MockObject {
		private MockProcessFlows flows;
	}

	@TestSource
	private static class ExternalManagedObjectSource extends AbstractManagedObjectSource<None, Indexed>
			implements ManagedObject {

		private ManagedObjectServiceContext<Indexed> context;

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Indexed> context) throws Exception {
			context.setObjectClass(this.getClass());
			context.addFlow(String.class);
		}

		@Override
		public void start(ManagedObjectExecuteContext<Indexed> context) throws Exception {
			this.context = new SafeManagedObjectService<>(context);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		@Override
		public Object getObject() throws Throwable {
			return null;
		}
	}

}
