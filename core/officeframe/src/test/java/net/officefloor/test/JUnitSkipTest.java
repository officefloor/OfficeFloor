/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.test;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Ensure {@link JUnit5Skip} is able to skip test.
 * 
 * @author Daniel Sagenschneider
 */
public class JUnitSkipTest {

	/**
	 * Trigger skip.
	 */
	@Order(1)
	public @RegisterExtension AfterEachCallback skip = new AfterEachCallback() {

		@Override
		public void afterEach(ExtensionContext context) throws Exception {
			JUnit5Skip.skip(context, "Should skip", null);
		}
	};

	/**
	 * Failure on start up to skip.
	 */
	@Order(2)
	public @RegisterExtension BeforeEachCallback setupFailure = new BeforeEachCallback() {

		@Override
		public void beforeEach(ExtensionContext context) throws Exception {
			throw new Exception("Test skipping after failing test setup");
		}
	};

	/**
	 * Test failing.
	 */
	@Test
	public void test() {
		fail("Should not be invoked");
	}

}
