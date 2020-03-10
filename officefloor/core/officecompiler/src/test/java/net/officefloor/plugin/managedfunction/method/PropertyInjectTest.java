package net.officefloor.plugin.managedfunction.method;

import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.Property;

/**
 * Ensure can inject {@link Property}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyInjectTest extends OfficeFrameTestCase {

	/**
	 * Ensure can inject {@link Property}.
	 */
	public void testProperty() {
		final String VALUE = "VALUE";
		PropertySection.propertyValue = null;
		MethodManagedFunctionBuilderUtil.runMethod(new PropertySection(), "method", (context) -> {
			// no type, as inject property
		}, (type) -> {
			// no type
		}, "property", VALUE);
		assertEquals("Incorrect property value", VALUE, PropertySection.propertyValue);
	}

	public static class PropertySection {

		private static String propertyValue;

		public void method(@Property("property") String value) {
			propertyValue = value;
		}
	}

}