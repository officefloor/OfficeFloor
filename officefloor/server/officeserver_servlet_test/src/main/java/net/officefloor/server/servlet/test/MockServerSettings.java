/*-
 * #%L
 * Provides testing using HttpServlet
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.server.servlet.test;

import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;

/**
 * Settings for running tests.
 * 
 * @author Daniel Sagenschneider
 */
public class MockServerSettings {

	/**
	 * {@link OfficeFloorExtensionService}.
	 */
	static OfficeFloorExtensionService officeFloorExtensionService = null;

	/**
	 * {@link OfficeExtensionService}.
	 */
	static OfficeExtensionService officeExtensionService = null;

	/**
	 * Logic to run within context.
	 */
	@FunctionalInterface
	public static interface WithinContext {
		void runInContext() throws Exception;
	}

	/**
	 * Runs {@link WithinContext} logic.
	 * 
	 * @param officeFloorExtension {@link OfficeFloorExtensionService}.
	 * @param officeExtension      {@link OfficeExtensionService}.
	 * @param logic                {@link WithinContext} logic.
	 * @throws Exception If failure with logic.
	 */
	public static void runWithinContext(OfficeFloorExtensionService officeFloorExtension,
			OfficeExtensionService officeExtension, WithinContext logic) throws Exception {
		officeFloorExtensionService = officeFloorExtension;
		officeExtensionService = officeExtension;
		try {
			logic.runInContext();
		} finally {
			officeFloorExtensionService = null;
			officeExtensionService = null;
		}
	}

}
