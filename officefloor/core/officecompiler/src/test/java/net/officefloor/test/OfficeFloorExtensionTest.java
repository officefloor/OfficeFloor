/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.test.Closure;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Tests the {@link OfficeFloorExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorExtensionTest implements OfficeExtensionService, OfficeExtensionServiceFactory {

	/**
	 * Reset for test setup.
	 */
	@BeforeAll
	public static void resetTest() {
		isLoadOffice = true;
		new OfficeFloorExtensionTest().cleanUpTest();
	}

	/**
	 * Reset for the next test.
	 */
	@AfterEach
	public void cleanUpTest() {
		failure = null;
		MockSection.value = null;
	}

	/**
	 * Stop participating in {@link OfficeFloor} configuration now tests are
	 * complete.
	 */
	@AfterAll
	public static void noFurtherConfiguration() {
		isLoadOffice = false;
	}

	/**
	 * Flags to load the {@link Office}.
	 */
	private static boolean isLoadOffice = false;

	/**
	 * Possible failure.
	 */
	private static Exception failure = null;

	/**
	 * {@link OfficeFloorExtension} under test.
	 */
	@RegisterExtension
	public final OfficeFloorExtension officeFloor = new OfficeFloorExtension();

	/**
	 * Ensure able to use {@link OfficeFloorExtension}.
	 */
	@Test
	public void testOfficeFloorExtension() throws Throwable {

		// Invoke the process
		final String PARAMETER = "TEST";
		this.officeFloor.invokeProcess("SECTION.function", PARAMETER);

		// Ensure appropriately triggered
		assertEquals(PARAMETER, MockSection.value, "Should invoke rule");
	}

	/**
	 * Ensure report failed compile.
	 */
	@Test
	public void testFailCompile() throws Throwable {

		// Setup failure
		failure = new Exception("TEST");

		// Load and trigger
		Closure<Boolean> isRuleRun = new Closure<>(false);
		Closure<Throwable> exception = new Closure<>();
		try {
			OfficeFloorExtension extension = new OfficeFloorExtension();
			extension.beforeAll(null);

			fail("Should not successfully compile");

		} catch (Error ex) {
			exception.value = ex;
		}

		// Should not run rule with failed compile
		assertFalse(isRuleRun.value, "Should not run rule");
		assertSame(failure, exception.value.getCause(), "Should propagate failure to test");
	}

	public static class MockSection {

		private static String value;

		public void function(@Parameter String argument) {
			value = argument;
		}
	}

	/*
	 * =================== OfficeExtensionService =====================
	 */

	@Override
	public OfficeExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		// Determine if load office
		if (!isLoadOffice) {
			return;
		}

		// Determine if fail compile
		if (failure != null) {
			throw failure;
		}

		// Add function
		officeArchitect.addOfficeSection("SECTION", ClassSectionSource.class.getName(), MockSection.class.getName());
	}

}