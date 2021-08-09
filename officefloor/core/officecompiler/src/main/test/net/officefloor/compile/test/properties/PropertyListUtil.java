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

package net.officefloor.compile.test.properties;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.test.JUnitAgnosticAssert;

/**
 * Utility class for testing a {@link PropertyList}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyListUtil {

	/**
	 * Validates the {@link Property} instances in the {@link PropertyList}.
	 * 
	 * @param propertyList       {@link PropertyList} to validate.
	 * @param propertyNameLabels Name/Label pair listing of expected
	 *                           {@link Property} instances in the
	 *                           {@link PropertyList}.
	 */
	public static void validatePropertyNameLabels(PropertyList propertyList, String... propertyNameLabels) {

		// Create the listing of properties
		List<Property> properties = new ArrayList<Property>();
		for (Property property : propertyList) {
			properties.add(property);
		}

		// Verify the properties
		JUnitAgnosticAssert.assertEquals(propertyNameLabels.length / 2, properties.size(),
				"Incorrect number of properties");
		for (int i = 0; i < propertyNameLabels.length; i += 2) {
			Property property = properties.get(i / 2);
			String name = propertyNameLabels[i];
			String label = propertyNameLabels[i + 1];
			JUnitAgnosticAssert.assertEquals(name, property.getName(), "Incorrect name for property " + i);
			JUnitAgnosticAssert.assertEquals(label, property.getLabel(), "Incorrect label for property " + i);
			JUnitAgnosticAssert.assertNull(property.getValue(), "Should be blank value for property " + (i / 2));
		}
	}

	/**
	 * Validates the {@link Property} values in the {@link PropertyList}.
	 * 
	 * @param propertyList       {@link PropertyList} to validate.
	 * @param propertyNameValues Name/value pair listing of expected
	 *                           {@link Property} instances in the
	 *                           {@link PropertyList}.
	 */
	public static void assertPropertyValues(PropertyList propertyList, String... propertyNameValues) {

		// Validate the properties
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			JUnitAgnosticAssert.assertEquals(value, propertyList.getPropertyValue(name, null),
					"Incorrect property " + name);
		}

		// Ensure no extra properties
		JUnitAgnosticAssert.assertEquals(propertyNameValues.length / 2, propertyList.getProperties().size(),
				"Incorrect number of properties");
	}

	/**
	 * All access via static methods.
	 */
	private PropertyListUtil() {
	}
}
