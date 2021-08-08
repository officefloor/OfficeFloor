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

package net.officefloor.test.skip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

/**
 * Tests the {@link SkipExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class SkipExtensionTest {

	/**
	 * Ensure skips running test.
	 */
	@Test
	public void skip() throws Throwable {
		try {
			new SkipExtension(true).beforeEach(null);
			fail("Should not be successful");
		} catch (TestAbortedException ex) {
			assertEquals("Assumption failed", ex.getMessage(), "Incorrect skip reason");
		} catch (Throwable ex) {
			fail(ex);
		}
	}

	/**
	 * Ensure provides details.
	 */
	@Test
	public void skipWithDescription() throws Throwable {
		try {
			new SkipExtension(true, "TEST SKIP").beforeEach(null);
			fail("Should not be successful");
		} catch (TestAbortedException ex) {
			assertEquals("Assumption failed: TEST SKIP", ex.getMessage(), "Incorrect skip reason");
		} catch (Throwable ex) {
			fail(ex);
		}
	}

	/**
	 * Ensure not skip test.
	 */
	@Test
	public void notSkip() throws Throwable {
		try {
			new SkipExtension(false).beforeEach(null);
		} catch (Throwable ex) {
			fail(ex);
		}
	}

}
