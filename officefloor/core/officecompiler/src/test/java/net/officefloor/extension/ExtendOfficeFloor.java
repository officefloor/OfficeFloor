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

package net.officefloor.extension;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionServiceFactory;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Provides convenience methods to compile an {@link OfficeFloor} with a single
 * {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExtendOfficeFloor implements OfficeFloorExtensionService, OfficeFloorExtensionServiceFactory {

	/**
	 * {@link OfficeFloorExtensionService} logic.
	 */
	private static OfficeFloorExtensionService extender = null;

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler;

	/**
	 * Instantiate.
	 */
	public ExtendOfficeFloor() {
		this.compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
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
	 * Compiles the {@link OfficeFloor}.
	 * 
	 * @param officeFloorConfiguration {@link OfficeFloorExtensionService} to
	 *                                 configure the {@link OfficeFloor}.
	 * @return {@link OfficeFloor}.
	 * @throws Exception If fails to compile the {@link OfficeFloor}.
	 */
	public OfficeFloor compileOfficeFloor(OfficeFloorExtensionService officeFloorConfiguration) throws Exception {

		// Compile the solution
		try {
			extender = officeFloorConfiguration;

			// Compile and return the OfficeFloor
			return this.compiler.compile("OfficeFloor");

		} finally {
			// Ensure the extender is cleared for other tests
			extender = null;
		}

	}

	/**
	 * Compiles and opens the {@link Office}.
	 * 
	 * @param officeFloorConfiguration {@link OfficeFloorExtensionService} to
	 *                                 configure the {@link OfficeFloor}.
	 * @return {@link OfficeFloor}.
	 * @throws Exception If fails to compile and open the {@link OfficeFloor}.
	 */
	public OfficeFloor compileAndOpenOfficeFloor(OfficeFloorExtensionService officeFloorConfiguration)
			throws Exception {

		// Compile the OfficeFloor
		OfficeFloor officeFloor = this.compileOfficeFloor(officeFloorConfiguration);

		// Open the OfficeFloor
		officeFloor.openOfficeFloor();

		// Return the OfficeFloor
		return officeFloor;
	}

	/*
	 * ====================== OfficeFloorExtensionService =====================
	 */

	@Override
	public OfficeFloorExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception {
		if (extender != null) {
			extender.extendOfficeFloor(officeFloorDeployer, context);
		}
	}

}
