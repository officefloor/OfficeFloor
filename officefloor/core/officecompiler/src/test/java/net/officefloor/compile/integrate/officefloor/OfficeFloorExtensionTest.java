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

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.extension.ExtendOfficeFloor;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Tests the {@link OfficeFloorExtensionService}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorExtensionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to extend the {@link OfficeFloor}.
	 */
	public void testExtendOfficeFloor() throws Exception {

		// Reset for test
		TestWork.dependency = null;

		// Compile the OfficeFloor with extension
		ExtendOfficeFloor extendOfficeFloor = new ExtendOfficeFloor();
		extendOfficeFloor.getOfficeFloorCompiler().setOfficeFloorSourceClass(TestOfficeFloorSource.class);
		OfficeFloor officeFloor = extendOfficeFloor.compileAndOpenOfficeFloor((deployer, context) -> {

			// Should have an Office registered
			DeployedOffice[] offices = deployer.getDeployedOffices();
			assertEquals("Incorrect number of offices", 1, offices.length);
			assertEquals("Incorrect office name", "OFFICE", offices[0].getDeployedOfficeName());

			// Should obtain Office
			DeployedOffice office = deployer.getDeployedOffice("OFFICE");
			assertSame("Incorrect office", offices[0], office);

			// Add the managed object
			OfficeFloorManagedObjectSource mos = deployer.addManagedObjectSource("MOS",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, CompileManagedObject.class.getName());
			mos.addOfficeFloorManagedObject("MO", ManagedObjectScope.THREAD);

			// Auto-wire the object to the office
			deployer.enableAutoWireObjects();
		});

		// Ensure able to invoke the function with the object from extension
		Office office = officeFloor.getOffice("OFFICE");
		office.getFunctionManager("MAIN.function").invokeProcess(null, null);
		assertNotNull("Should have loaded object from extension", TestWork.dependency);
	}

	public static class CompileManagedObject {
	}

	public static class TestWork {

		public static CompileManagedObject dependency;

		public void function(CompileManagedObject object) {
			dependency = object;
		}
	}

	@TestSource
	public static class TestOfficeFloorSource extends AbstractOfficeFloorSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void specifyConfigurationProperties(RequiredProperties requiredProperties,
				OfficeFloorSourceContext context) throws Exception {
		}

		@Override
		public void sourceOfficeFloor(OfficeFloorDeployer deployer, OfficeFloorSourceContext context) throws Exception {
			deployer.addDeployedOffice("OFFICE", TestOfficeSource.class.getName(), null);
		}
	}

	@TestSource
	public static class TestOfficeSource extends AbstractOfficeSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext context) throws Exception {
			OfficeSection section = officeArchitect.addOfficeSection("MAIN", ClassSectionSource.class.getName(),
					TestWork.class.getName());
			OfficeSectionObject object = section.getOfficeSectionObject(CompileManagedObject.class.getName());
			OfficeObject dependency = officeArchitect.addOfficeObject("DEPENDENCY",
					CompileManagedObject.class.getName());
			officeArchitect.link(object, dependency);
		}
	}

}
