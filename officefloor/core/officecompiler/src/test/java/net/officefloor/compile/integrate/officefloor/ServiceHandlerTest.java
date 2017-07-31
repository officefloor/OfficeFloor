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
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Ensure can configure service handling for a {@link DeployedOffice}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServiceHandlerTest extends OfficeFrameTestCase {

	/**
	 * Indicates if internally triggered.
	 */
	private static boolean isInternallyTriggered = false;

	/**
	 * Value sent from invoker.
	 */
	private static Object value = null;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * {@link FunctionManager} to externally trigger.
	 */
	private FunctionManager externalTriggerFunction;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		final String OFFICE_NAME = "OFFICE";
		final MockOfficeSource officeSource = new MockOfficeSource();

		// Compile the OfficeFloor with extension
		CompileOfficeFloor compile = new CompileOfficeFloor();

		// Add the Office
		compile.officeFloor((context) -> {
			// Add the deployed office
			context.getOfficeFloorDeployer().addDeployedOffice(OFFICE_NAME, officeSource, null);
		});

		// Extend the existing Office
		compile.officeFloor((context) -> {

			// Obtain the existing office (added above)
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
			DeployedOffice office = deployer.getDeployedOffice(OFFICE_NAME);
			assertNotNull("Should have existing Office", office);

			// Obtain the servicing input
			DeployedOfficeInput input = office.getDeployedOfficeInput("SECTION", "service");

			// Internally invoke from the managed object
			OfficeFloorManagedObjectSource mos = deployer.addManagedObjectSource("MOS",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, MockObject.class.getName());
			deployer.link(mos.getManagingOffice(), office);
			deployer.link(mos, deployer.addInputManagedObject("INPUT"));
			deployer.link(mos.getManagedObjectFlow("doProcess"), input);
			mos.addOfficeFloorManagedObject("MOS", ManagedObjectScope.PROCESS);

			// Capture function manager (for external triggering)
			this.externalTriggerFunction = input.getFunctionManager();
		});

		// Open the OfficeFloor
		this.officeFloor = compile.compileAndOpenOfficeFloor();

		// Reset for testing
		isInternallyTriggered = false;
		value = null;
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure able to trigger internally via {@link ManagedObjectSource}.
	 */
	public void testInternallyTriggered() throws Exception {

		// Obtain the function to trigger via managed object
		FunctionManager internalTrigger = this.officeFloor.getOffice("OFFICE").getFunctionManager("SECTION.trigger");

		// Internally trigger process
		internalTrigger.invokeProcess("INTERNAL", null);

		// Ensure internally trigger and appropriately services
		assertTrue("Should be internally triggered", isInternallyTriggered);
		assertEquals("Incorrect service value", "INTERNAL", value);
	}

	/**
	 * Ensure able to trigger externally via {@link FunctionManager}.
	 */
	public void testExtendDeployedOffice() throws Exception {

		// Externally trigger process
		this.externalTriggerFunction.invokeProcess("EXTERNAL", null);

		// Ensure externally trigger and appropriately services
		assertFalse("Should be externally triggered", isInternallyTriggered);
		assertEquals("Incorrect service value", "EXTERNAL", value);
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

	@FlowInterface
	public static interface MockProcessFlows {
		void doProcess(Object parameter);
	}

	public static class MockObject {
		private MockProcessFlows flows;

		public void trigger(Object parameter) {
			this.flows.doProcess(parameter);
		}
	}

	public static class MockSection {

		public void trigger(@Parameter Object parameter, MockObject object) {
			isInternallyTriggered = true;
			object.trigger(parameter);
		}

		public void service(@Parameter Object parameter) {
			value = parameter;
		}
	}

}