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