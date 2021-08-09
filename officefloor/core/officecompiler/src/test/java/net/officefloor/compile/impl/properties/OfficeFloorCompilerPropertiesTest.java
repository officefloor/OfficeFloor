/*-
 * #%L
 * OfficeCompiler
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
		Closure<String> sectionProperty = new Closure<>();

		// Ensure property is available
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.getOfficeFloorCompiler().addProperty("OFFICE.property", PROPERTY_VALUE);
		compile.getOfficeFloorCompiler().addProperty("OFFICE.SECTION.property", PROPERTY_VALUE);
		compile.officeFloor((context) -> {
			officeFloorProperty.value = context.getOfficeFloorSourceContext().getProperty("OFFICE.property");
		});
		compile.office((context) -> {
			officeProperty.value = context.getOfficeSourceContext().getProperty("property");
		});
		compile.section((context) -> {
			sectionProperty.value = context.getSectionSourceContext().getProperty("property");
		});
		assertNotNull("Should compile", compile.compileOfficeFloor());

		// Ensure properties passed through
		assertEquals("Incorrect OfficeFloor property", PROPERTY_VALUE, officeFloorProperty.value);
		assertEquals("Incorrect Office property", PROPERTY_VALUE, officeProperty.value);
		assertEquals("Incorrect Section property", PROPERTY_VALUE, sectionProperty.value);
	}

}
