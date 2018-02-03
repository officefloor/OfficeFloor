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
package net.officefloor.compile.integrate.officefloor;

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
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.NameAwareManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Ensure can configure service handling for a {@link DeployedOffice}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServicingInputTest extends OfficeFrameTestCase {

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

	@Override
	protected void setUp() throws Exception {
		super.setUp();

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
			assertNotNull("Should have existing Office", office);

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

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can invoke via external {@link FunctionManager}.
	 */
	public void testExternalFunctionManager() throws Exception {
		FunctionManager function = this.officeFloor.getOffice("OFFICE")
				.getFunctionManager("SECTION.externalFunctionManager");
		function.invokeProcess("EXTERNAL_FUNCTION_MANAGER", null);
		assertEquals("Incorrect value", "EXTERNAL_FUNCTION_MANAGER", value);
	}

	/**
	 * Ensure can invoke via internal {@link FunctionManager}.
	 */
	public void testInternalFunctionManager() throws Exception {
		this.internalFunctionManager.invokeProcess("INTERNAL_FUNCTION_MANAGER", null);
		assertEquals("Incorrect value", "INTERNAL_FUNCTION_MANAGER", value);
	}

	/**
	 * Ensure can invoke via {@link ExternalServiceInput}.
	 */
	public void testExternalServiceInput() throws Exception {
		ServiceInputObject serviceObject = new ServiceInputObject();
		this.externalServiceInput.service(serviceObject, null);
		assertEquals("Incorrect value", "EXTERNAL_SERVICE_INPUT", value);
		assertEquals("Incorrect service input servicing",
				"ExternalServiceInput_" + ServiceInputObject.class.getName() + ":EXTERNAL_SERVICE_INPUT",
				serviceObject.value);
	}

	/**
	 * Ensure can invoke internally via {@link ManagedObjectSource}.
	 */
	public void testManagedObjectInternalThread() throws Exception {
		FunctionManager function = this.officeFloor.getOffice("OFFICE").getFunctionManager("SECTION.trigger");
		function.invokeProcess("INTERNAL_MANAGED_OBJECT", null);
		assertEquals("Incorrect value", "INTERNAL_MANAGED_OBJECT", value);
	}

	/**
	 * Ensure can invoke externally via {@link ManagedObjectSource}.
	 */
	public void testManagedObjectExternalThread() throws Exception {
		this.externalManagdObjectSource.context.invokeProcess(0, "EXTERNAL_MANAGED_OBJECT",
				this.externalManagdObjectSource, 0, null);
		assertEquals("Incorrect value", "EXTERNAL_MANAGED_OBJECT", value);
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

		@NextFunction("service")
		public String externalFunctionManager(@Parameter String parameter) {
			return parameter;
		}

		@NextFunction("service")
		public String internalFunctionManager(@Parameter String parameter) {
			return parameter;
		}

		@NextFunction("service")
		public String externalServiceInput() {
			return "EXTERNAL_SERVICE_INPUT";
		}

		public void trigger(@Parameter String parameter, MockObject object) {
			object.flows.doProcess(parameter);
		}

		@NextFunction("service")
		public String managedObjectInternalThread(@Parameter String parameter) {
			return parameter;
		}

		@NextFunction("service")
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

	private static class ServiceInputObject implements NameAwareManagedObject {

		private String name;

		private String value = null;

		@Override
		public void setBoundManagedObjectName(String boundManagedObjectName) {
			this.name = boundManagedObjectName;
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

		private ManagedObjectExecuteContext<Indexed> context;

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
			this.context = context;
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