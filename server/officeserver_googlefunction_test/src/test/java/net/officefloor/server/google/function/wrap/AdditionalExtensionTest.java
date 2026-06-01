/*-
 * #%L
 * Google Function Testing
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
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

package net.officefloor.server.google.function.wrap;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.google.function.mock.MockGoogleHttpFunctionExtension;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Ensure can add additional {@link OfficeFloorExtensionService} and
 * {@link OfficeExtensionService}.
 */
public class AdditionalExtensionTest {

	private boolean isOfficeFloorExtended = false;

	private boolean isOfficeExtended = false;

	public final @RegisterExtension @Order(0) MockGoogleHttpFunctionExtension httpFunction = new MockGoogleHttpFunctionExtension(
			TestHttpFunction.class).officeFloor((deployer, context) -> {
				AdditionalExtensionTest.this.isOfficeFloorExtended = true;
			}).office((architect, context) -> {
				AdditionalExtensionTest.this.isOfficeExtended = true;
			});

	public final @RegisterExtension @Order(1) OfficeFloorExtension officeFloor = new OfficeFloorExtension();

	/**
	 * Ensure can extend {@link OfficeFloor}.
	 */
	@Test
	public void officeFloor() {
		assertTrue(this.isOfficeFloorExtended, "Should extend OfficeFloor");
	}

	/**
	 * Ensure can extend {@link Office}.
	 */
	@Test
	public void office() {
		assertTrue(this.isOfficeExtended, "Should extend Office");
	}

}
