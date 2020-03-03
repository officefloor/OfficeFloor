package net.officefloor.compile.test.systemproperties;

import java.util.function.Function;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link SystemPropertiesRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class SystemPropertiesRuleTest extends OfficeFrameTestCase {

	/**
	 * Ensure correctly setting/unsetting system properties.
	 */
	public void testSystemProperties() {

		// Determine an empty property name
		Function<String, String> emptyPropertyName = (prefix) -> {
			String name = prefix;
			int index = 0;
			String value = "check";
			do {
				value = System.getProperty(name);
				if (value != null) {
					index++;
					name = prefix + index;
				}
			} while (value != null);
			return name;
		};

		// Find non-configured properties
		String propertyOneName = emptyPropertyName.apply("PropertyOne");
		String propertyTwoName = emptyPropertyName.apply("PropertyTwo");
		try {

			// Provide one property value
			final String originalValue = "ORIGINAL";
			System.setProperty(propertyOneName, originalValue);

			// Run rule to ensure appropriately sets value
			final String overrideValue = "OVERRIDE";
			final String setValue = "SET";
			new SystemPropertiesRule(propertyOneName, overrideValue, propertyTwoName, setValue).run(() -> {
				assertEquals("Incorrect override value", overrideValue, System.getProperty(propertyOneName));
				assertEquals("Incorrect set value", setValue, System.getProperty(propertyTwoName));
			});

			// Ensure unsets
			assertEquals("Incorrect unset value", originalValue, System.getProperty(propertyOneName));
			assertNull("Should clear value", System.getProperty(propertyTwoName));

		} finally {
			// Ensure reset
			System.clearProperty(propertyOneName);
			System.clearProperty(propertyTwoName);
		}
	}

}