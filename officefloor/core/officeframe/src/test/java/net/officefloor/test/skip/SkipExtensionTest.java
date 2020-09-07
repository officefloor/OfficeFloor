/*-
 * #%L
 * OfficeFrame
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
