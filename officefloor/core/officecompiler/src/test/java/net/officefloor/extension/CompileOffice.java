/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.extension;

import org.junit.Assert;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * <p>
 * {@link OfficeExtensionService} to configure the {@link Office} within tests.
 * <p>
 * For this to operate within tests, the
 * <code>src/test/resources/META-INF/services/net.officefloor.compile.spi.office.extension.OfficeExtensionService</code>
 * must be configured with this class name.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileOffice implements OfficeExtensionService, OfficeExtensionServiceFactory {

	/**
	 * {@link OfficeExtensionService} logic.
	 */
	private static OfficeExtensionService extender = null;

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler;

	/**
	 * Instantiate.
	 */
	public CompileOffice() {
		this(OfficeFloorCompiler.newOfficeFloorCompiler(null));
	}

	/**
	 * Instantiate.
	 * 
	 * @param officeFloorCompiler {@link OfficeFloorCompiler} to use.
	 */
	public CompileOffice(OfficeFloorCompiler officeFloorCompiler) {
		this.compiler = officeFloorCompiler;
		this.compiler.setCompilerIssues(new FailTestCompilerIssues());
	}

	/**
	 * Obtains the {@link OfficeFloorCompiler}.
	 * 
	 * @return {@link OfficeFloorCompiler}.
	 */
	public OfficeFloorCompiler getOfficeFloorCompiler() {
		return this.compiler;
	}

	/**
	 * Compiles the {@link Office}.
	 * 
	 * @param officeConfiguration {@link OfficeExtensionService} to configure the
	 *                            {@link Office}.
	 * @return {@link OfficeFloor}.
	 * @throws Exception If fails to compile the {@link OfficeFloor}.
	 */
	public OfficeFloor compileOffice(OfficeExtensionService officeConfiguration) throws Exception {

		// Compile the solution
		try {
			extender = officeConfiguration;

			// Compile and return the office
			return this.compiler.compile("OfficeFloor");

		} finally {
			// Ensure the extender is cleared for other tests
			extender = null;
		}

	}

	/**
	 * Compiles and opens the {@link Office}.
	 * 
	 * @param officeConfiguration {@link OfficeExtensionService} to configure the
	 *                            {@link Office}.
	 * @return {@link OfficeFloor}.
	 * @throws Exception If fails to compile and open the {@link OfficeFloor}.
	 */
	public OfficeFloor compileAndOpenOffice(OfficeExtensionService officeConfiguration) throws Exception {

		// Compile the OfficeFloor
		OfficeFloor officeFloor = this.compileOffice(officeConfiguration);

		// Open the OfficeFloor
		Assert.assertNotNull("Should compile OfficeFloor", officeFloor);
		officeFloor.openOfficeFloor();

		// Return the OfficeFloor
		return officeFloor;
	}

	/*
	 * ====================== OfficeExtensionService =====================
	 */

	@Override
	public OfficeExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {
		if (extender != null) {
			extender.extendOffice(officeArchitect, context);
		}
	}

}
