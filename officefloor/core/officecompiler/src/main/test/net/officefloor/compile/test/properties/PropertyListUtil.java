package net.officefloor.compile.test.properties;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;

/**
 * Utility class for testing a {@link PropertyList}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyListUtil {

	/**
	 * Validates the {@link Property} instances in the {@link PropertyList}.
	 * 
	 * @param propertyList
	 *            {@link PropertyList} to validate.
	 * @param propertyNameLabels
	 *            Name/Label pair listing of expected {@link Property} instances
	 *            in the {@link PropertyList}.
	 */
	public static void validatePropertyNameLabels(PropertyList propertyList,
			String... propertyNameLabels) {

		// Create the listing of properties
		List<Property> properties = new ArrayList<Property>();
		for (Property property : propertyList) {
			properties.add(property);
		}

		// Verify the properties
		TestCase.assertEquals("Incorrect number of properties",
				propertyNameLabels.length / 2, properties.size());
		for (int i = 0; i < propertyNameLabels.length; i += 2) {
			Property property = properties.get(i / 2);
			String name = propertyNameLabels[i];
			String label = propertyNameLabels[i + 1];
			TestCase.assertEquals("Incorrect name for property " + i, name,
					property.getName());
			TestCase.assertEquals("Incorrect label for property " + i, label,
					property.getLabel());
			TestCase.assertNull(
					"Should be blank value for property " + (i / 2),
					property.getValue());
		}
	}

	/**
	 * All access via static methods.
	 */
	private PropertyListUtil() {
	}
}