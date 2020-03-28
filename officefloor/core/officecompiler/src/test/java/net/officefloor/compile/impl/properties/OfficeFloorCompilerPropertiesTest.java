package net.officefloor.compile.impl.properties;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure {@link Property} instances are passed through.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCompilerPropertiesTest extends OfficeFrameTestCase {

	/**
	 * Ensure the {@link OfficeFloorCompiler} {@link Property} instances are made
	 * available to {@link OfficeFloor} and if appropriate {@link Office}.
	 */
	public void testCompilerProperties() throws Exception {

		// Capture the values
		final String PROPERTY_VALUE = "PASS_THROUGH";
		Closure<String> officeFloorProperty = new Closure<>();
		Closure<String> officeProperty = new Closure<>();

		// Ensure property is available
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.getOfficeFloorCompiler().addProperty("OFFICE.property", PROPERTY_VALUE);
		compile.officeFloor((context) -> {
			officeFloorProperty.value = context.getOfficeFloorSourceContext().getProperty("OFFICE.property");
		});
		compile.office((context) -> {
			officeProperty.value = context.getOfficeSourceContext().getProperty("property");
		});
		assertNotNull("Should compile", compile.compileOfficeFloor());

		// Ensure properties passed through
		assertEquals("Incorrect OfficeFloor property", PROPERTY_VALUE, officeFloorProperty.value);
		assertEquals("Incorrect Office property", PROPERTY_VALUE, officeProperty.value);
	}

}