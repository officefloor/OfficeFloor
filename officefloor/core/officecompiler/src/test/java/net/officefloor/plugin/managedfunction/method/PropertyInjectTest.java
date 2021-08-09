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
