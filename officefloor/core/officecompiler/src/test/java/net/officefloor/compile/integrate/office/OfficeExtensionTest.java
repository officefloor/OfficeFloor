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

package net.officefloor.compile.integrate.office;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.extension.CompileOffice;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Tests the {@link OfficeExtensionService}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeExtensionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to extend the {@link Office}.
	 */
	public void testExtendOffice() throws Exception {

		// Reset for test
		SourceFunction.isInvoked = false;
		ExtendFunction.isInvoked = false;

		// Compile the office with extension
		CompileOffice compileOffice = new CompileOffice();
		compileOffice.getOfficeFloorCompiler().setOfficeFloorSourceClass(TestOfficeFloorSource.class);
		OfficeFloor officeFloor = compileOffice.compileAndOpenOffice((architect, context) -> {

			// Ensure correct office name
			assertEquals("Incorrect Office name", "OFFICE", context.getOfficeName());

			// Extend with managed function
			architect.addOfficeSection("EXTEND", ClassSectionSource.class.getName(), ExtendFunction.class.getName());
		});

		// Ensure able to invoke the functions from the Office
		Office office = officeFloor.getOffice("OFFICE");

		// Ensure able to invoke function added by source
		office.getFunctionManager("MAIN.function").invokeProcess(null, null);
		assertTrue("Source function should be invoked", SourceFunction.isInvoked);

		// Ensure able to invoke function added by extension
		office.getFunctionManager("EXTEND.function").invokeProcess(null, null);
		assertTrue("Extend function should be invoked", ExtendFunction.isInvoked);
	}

	public static class SourceFunction {

		public static boolean isInvoked = false;

		public void function() {
			isInvoked = true;
		}
	}

	public static class ExtendFunction {

		public static boolean isInvoked = false;

		public void function() {
			isInvoked = true;
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
			officeArchitect.addOfficeSection("MAIN", ClassSectionSource.class.getName(),
					SourceFunction.class.getName());
		}
	}

}
