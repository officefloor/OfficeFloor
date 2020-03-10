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
