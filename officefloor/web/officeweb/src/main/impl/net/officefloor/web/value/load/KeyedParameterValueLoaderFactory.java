package net.officefloor.web.value.load;

import java.lang.reflect.Method;
import java.util.Map;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.build.HttpValueLocation;

/**
 * {@link StatelessValueLoaderFactory} to load keyed string parameter.
 * 
 * @author Daniel Sagenschneider
 */
public class KeyedParameterValueLoaderFactory implements StatelessValueLoaderFactory {

	/**
	 * Property name.
	 */
	private final String propertyName;

	/**
	 * {@link Method} name.
	 */
	private final String methodName;

	/**
	 * Initiate.
	 * 
	 * @param propertyName
	 *            Property name.
	 * @param methodName
	 *            {@link Method} name.
	 */
	public KeyedParameterValueLoaderFactory(String propertyName, String methodName) {
		this.propertyName = propertyName;
		this.methodName = methodName;
	}

	/*
	 * ===================== StatelessValueLoaderFactory =================
	 */

	@Override
	public String getPropertyName() {
		return this.propertyName;
	}

	@Override
	public StatelessValueLoader createValueLoader(Class<?> clazz) throws Exception {

		// Obtain the loader method
		final Method loaderMethod = clazz.getMethod(this.methodName, String.class, String.class);

		// Obtain the value location
		final HttpValueLocation loaderLocation = ValueLoaderSource.getLocation(loaderMethod);

		// Return the new value loader
		return new StatelessValueLoader() {
			@Override
			public void loadValue(Object object, String name, int nameIndex, String value, HttpValueLocation location,
					Map<PropertyKey, Object> state) throws HttpException {

				// Determine if match location
				if (!ValueLoaderSource.isLocationMatch(loaderLocation, location)) {
					return; // not match, so do not load
				}

				// Obtain the keyed value
				int keyEnd = name.indexOf('}', nameIndex);
				if (keyEnd < 0) {
					return; // No key so do not load
				}
				String key = name.substring(nameIndex, keyEnd);

				// Load the value
				ValueLoaderSource.loadValue(object, loaderMethod, key, value);
			}
		};
	}
}