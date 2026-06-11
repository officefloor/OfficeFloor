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

package net.officefloor.server.google.function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.google.function.mock.MockGoogleHttpFunctionExtension;

/**
 * Tests the {@link OfficeFloorHttpFunction}.
 */
public class OfficeFloorHttpFunctionTest {

	public static final @RegisterExtension MockGoogleHttpFunctionExtension httpFunction = SimpleRequestTestHelper
			.loadApplication(new MockGoogleHttpFunctionExtension());

	@BeforeEach
	public void openOfficeFloor() throws Exception {
		OfficeFloorHttpFunction.open();
	}

	@AfterEach
	public void closeOfficeFloor() throws Exception {
		OfficeFloorHttpFunction.close();
	}

	/**
	 * Ensure can request.
	 */
	@Test
	public void simpleRequest() {
		SimpleRequestTestHelper.assertMockRequest(httpFunction.getMockHttpServer());
	}

}
