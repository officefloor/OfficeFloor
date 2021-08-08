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

package net.officefloor.frame.impl.execute.function;

import java.util.logging.Logger;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;

/**
 * Tests the {@link Logger} available from the {@link ManagedFunctionContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionLoggerTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure appropriate {@link Logger}.
	 */
	public void testLogger() throws Exception {

		// Capture office name
		final String officeName = this.getOfficeName();

		// Create the managed function
		final String FUNCTION_NAME = "FUNCTION_LOGGER";
		Closure<Logger> logger = new Closure<>();
		this.constructFunction(FUNCTION_NAME, () -> (context) -> {
			logger.value = context.getLogger();
		});

		// Ensure provide appropriate logger
		try (OfficeFloor officeFloor = this.constructOfficeFloor()) {
			officeFloor.openOfficeFloor();

			// Invoke function and confirm correct logger
			officeFloor.getOffice(officeName).getFunctionManager(FUNCTION_NAME).invokeProcess(null, null);

			// Ensure correct logger
			assertNotNull("Should have logger", logger.value);
			assertEquals("Incorrect logger", FUNCTION_NAME, logger.value.getName());
		}
	}

}
