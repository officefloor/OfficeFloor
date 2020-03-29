package net.officefloor.servlet.inject;

import java.lang.reflect.Field;

/**
 * Factory for creation of dependency from {@link Field}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FieldDependencyExtractor {

	/**
	 * Extracts the {@link RequiredDependency}.
	 * 
	 * @param field {@link Field}.
	 * @return {@link RequiredDependency}.
	 */
	RequiredDependency extractRequiredDependency(Field field);

}