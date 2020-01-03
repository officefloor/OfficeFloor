package net.officefloor.web.value.load;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.value.load.PropertyKey;
import net.officefloor.web.value.load.PropertyKeyFactory;
import net.officefloor.web.value.load.StatelessValueLoader;

/**
 * Root {@link StatelessValueLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class RootStatelessValueLoader implements StatelessValueLoader {

	/**
	 * {@link StatelessValueLoader} instances by {@link PropertyKey}.
	 */
	private Map<PropertyKey, StatelessValueLoader> valueLoaders = new HashMap<PropertyKey, StatelessValueLoader>();

	/**
	 * {@link PropertyKeyFactory}.
	 */
	private final PropertyKeyFactory propertyKeyFactory;

	/**
	 * Initiate.
	 * 
	 * @param valueLoaders
	 *            {@link StatelessValueLoader} instances by {@link PropertyKey}.
	 * @param propertyKeyFactory
	 *            {@link PropertyKeyFactory}.
	 */
	public RootStatelessValueLoader(Map<PropertyKey, StatelessValueLoader> valueLoaders,
			PropertyKeyFactory propertyKeyFactory) {
		this.valueLoaders = valueLoaders;
		this.propertyKeyFactory = propertyKeyFactory;
	}

	/*
	 * ================== StatelessValueLoader ==========================
	 */

	@Override
	public void loadValue(Object object, String name, int nameIndex, String value, HttpValueLocation location,
			Map<PropertyKey, Object> state) throws HttpException {

		// Parse out the property name (start at name index)
		int index = -1;
		SEPARATOR_FOUND: for (int i = nameIndex; i < name.length(); i++) {
			char character = name.charAt(i);
			switch (character) {
			case '.':
			case '{':
				index = i;
				break SEPARATOR_FOUND;
			default:
				// not separator so continue to next character
			}
		}

		// Split out the property name from the name
		String propertyName;
		if (index < 0) {
			// Entire name
			propertyName = name.substring(nameIndex);
			nameIndex = name.length(); // end of name
		} else {
			// Not entire name
			propertyName = name.substring(nameIndex, index);
			nameIndex = index + 1; // ignore separator ('.' character)
		}

		// Create the property key
		PropertyKey propertyKey = this.propertyKeyFactory.createPropertyKey(propertyName);

		// Obtain the value loader for the property name
		StatelessValueLoader valueLoader = this.valueLoaders.get(propertyKey);
		if (valueLoader == null) {
			return; // no value loader for property
		}

		// Load the value
		valueLoader.loadValue(object, name, nameIndex, value, location, state);
	}

}