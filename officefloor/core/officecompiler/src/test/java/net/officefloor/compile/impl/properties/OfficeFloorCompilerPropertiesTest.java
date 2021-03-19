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
