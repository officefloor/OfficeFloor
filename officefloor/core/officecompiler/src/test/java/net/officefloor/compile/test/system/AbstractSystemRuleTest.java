package net.officefloor.compile.test.system;

import java.util.function.Function;

import net.officefloor.compile.test.system.SystemPropertiesRule;
import net.officefloor.compile.test.system.AbstractSystemRule.ContextRunnable;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link SystemPropertiesRule}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSystemRuleTest extends OfficeFrameTestCase {

	/**
	 * Obtains the value.
	 * 
	 * @param name Name of value.
	 * @return Value.
	 */
	protected abstract String get(String name);

	/**
	 * Specifies the value.
	 * 
	 * @param name  Name for value.
	 * @param value Value.
	 */
	protected abstract void set(String name, String value);

	/**
	 * Clears the value.
	 * 
	 * @param name Name of value.
	 */
	protected abstract void clear(String name);

	/**
	 * Undertakes the test.
	 * 
	 * @param logic    Logic to be wrapped with changes.
	 * @param nameOne  First name.
	 * @param valueOne First value.
	 * @param nameTwo  Second name.
	 * @param valueTwo Second value.
	 */
	protected abstract void doTest(ContextRunnable<Exception> logic, String nameOne, String valueOne, String nameTwo,
			String valueTwo) throws Exception;

	/**
	 * Ensure correctly setting/unsetting properties.
	 */
	public void testOverride() throws Exception {

		// Determine an empty property name
		Function<String, String> emptyPropertyName = (prefix) -> {
			String name = prefix;
			int index = 0;
			String value = "check";
			do {
				value = this.get(name);
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
			this.set(propertyOneName, originalValue);
			assertEquals("INVALID TEST: failed to set value " + propertyOneName, originalValue,
					this.get(propertyOneName));

			// Run rule to ensure appropriately sets value
			final String overrideValue = "OVERRIDE";
			final String setValue = "SET";
			this.doTest(() -> {
				assertEquals("Incorrect override value", overrideValue, this.get(propertyOneName));
				assertEquals("Incorrect set value", setValue, this.get(propertyTwoName));
			}, propertyOneName, overrideValue, propertyTwoName, setValue);

			// Ensure unsets
			assertEquals("Incorrect unset value", originalValue, this.get(propertyOneName));
			assertNull("Should clear value", this.get(propertyTwoName));

		} finally {
			// Ensure reset
			this.clear(propertyOneName);
			this.clear(propertyTwoName);
		}
	}

}