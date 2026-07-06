/*-
 * #%L
 * Google Function OfficeFloor Server
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

package net.officefloor.server.google.function.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.google.function.OfficeFloorHttpFunction;
import net.officefloor.server.google.function.SimpleRequestTestHelper;
import net.officefloor.server.google.function.officefloor.OfficeFloorHttpFunctionReference;

/**
 * Tests default will load {@link OfficeFloorHttpFunction}.
 */
public class GoogleHttpFunctionExtensionTest {

	private static final @RegisterExtension GoogleHttpFunctionExtension httpFunction = SimpleRequestTestHelper
			.loadApplication(new GoogleHttpFunctionExtension());

	@BeforeEach
	public void openOfficeFloor() throws Exception {
		OfficeFloorHttpFunction.open();
	}

	@AfterEach
	public void closeOfficeFloor() throws Exception {
		OfficeFloorHttpFunction.close();
	}

	/**
	 * Ensure using correct {@link Class} name.
	 */
	@Test
	public void ensureCorrectClassName() {
		assertEquals(OfficeFloorHttpFunction.class.getName(),
				OfficeFloorHttpFunctionReference.OFFICEFLOOR_HTTP_FUNCTION_CLASS_NAME,
				"Incorrect " + OfficeFloorHttpFunction.class.getSimpleName() + " class name");
	}

	/**
	 * Ensure servicing with {@link OfficeFloorHttpFunction}.
	 */
	@Test
	public void request() {
		SimpleRequestTestHelper.assertRequest();
	}

	/**
	 * Ensure servicing secure with {@link OfficeFloorHttpFunction}.
	 */
	@Test
	public void requestSecure() {
		SimpleRequestTestHelper.assertSecureRequest();
	}

}
