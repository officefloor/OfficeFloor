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

package net.officefloor.server.google.function.test;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.server.google.function.wrap.TestHttpFunction;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Ensure can run {@link HttpFunction} on alternate port.
 */
public class AlternatePortTest extends AbstractGoogleHttpFunctionTestCase {

	private static final int ALTERNATE_HTTP_PORT = 8787;

	private static final int ALTERNATE_HTTPS_PORT = 8788;

	public final @RegisterExtension @Order(0) GoogleHttpFunctionExtension httpFunction = new GoogleHttpFunctionExtension(
			TestHttpFunction.class).httpPort(ALTERNATE_HTTP_PORT).httpsPort(ALTERNATE_HTTPS_PORT);

	public final @RegisterExtension @Order(1) OfficeFloorExtension officeFloor = new OfficeFloorExtension();

	/**
	 * Ensure can request.
	 */
	@Test
	public void request() throws Exception {
		this.doTest(false, ALTERNATE_HTTP_PORT);
	}

	/**
	 * Ensure can secure request.
	 */
	@Test
	public void requestSecure() throws Exception {
		this.doTest(true, ALTERNATE_HTTPS_PORT);
	}

}
