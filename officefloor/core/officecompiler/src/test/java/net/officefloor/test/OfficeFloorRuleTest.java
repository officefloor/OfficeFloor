package net.officefloor.test;

import org.junit.runners.model.Statement;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Tests the {@link OfficeFloorRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorRuleTest extends OfficeFrameTestCase implements OfficeExtensionService {

	/**
	 * Flags to load the {@link Office}.
	 */
	private static boolean isLoadOffice = false;

	/**
	 * Possible failure.
	 */
	private static Exception failure = null;

	/**
	 * Ensure able to use {@link OfficeFloorRule}.
	 */
	public void testOfficeFloorRule() throws Throwable {
		isLoadOffice = true;
		failure = null;
		try {

			final String PARAMETER = "TEST";

			// Load and trigger function
			MockSection.value = null;
			OfficeFloorRule rule = new OfficeFloorRule();
			rule.apply(new Statement() {

				@Override
				public void evaluate() throws Throwable {

					// Ensure can invoke section
					rule.invokeProcess("SECTION.function", PARAMETER);
				}
			}, null).evaluate();

			// Ensure appropriately triggered
			assertEquals("Should invoke rule", PARAMETER, MockSection.value);

		} finally {
			isLoadOffice = false;
		}
	}

	/**
	 * Ensure report failed compile.
	 */
	public void testFailCompile() throws Throwable {
		isLoadOffice = true;
		failure = new Exception("TEST");
		try {

			Closure<Boolean> isRuleRun = new Closure<>(false);

			// Load and trigger
			try {
				OfficeFloorRule rule = new OfficeFloorRule();
				rule.apply(new Statement() {

					@Override
					public void evaluate() throws Throwable {
						isRuleRun.value = true;
					}
				}, null).evaluate();

				fail("Should not successfully compile");

			} catch (Error ex) {
				// Should throw error
			}

			// Should not run rule with failed compile
			assertFalse("Should not run rule", isRuleRun.value);

		} finally {
			isLoadOffice = false;
		}
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