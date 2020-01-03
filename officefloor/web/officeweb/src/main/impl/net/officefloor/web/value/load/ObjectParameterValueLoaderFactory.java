package net.officefloor.web.value.load;

import java.lang.reflect.Method;
import java.util.Map;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.value.load.ObjectInstantiator;
import net.officefloor.web.value.load.PropertyKey;
import net.officefloor.web.value.load.PropertyKeyFactory;
import net.officefloor.web.value.load.StatelessValueLoader;
import net.officefloor.web.value.load.StatelessValueLoaderFactory;

/**
 * {@link StatelessValueLoaderFactory} to load an object parameter.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectParameterValueLoaderFactory implements StatelessValueLoaderFactory {

	/**
	 * Property name.
	 */
	private final String propertyName;

	/**
	 * {@link Method} name.
	 */
	private final String methodName;

	/**
	 * Object type.
	 */
	private final Class<?> objectType;

	/**
	 * {@link ObjectInstantiator}.
	 */
	private final ObjectInstantiator objectInstantiator;

	/**
	 * {@link PropertyKeyFactory}.
	 */
	private final PropertyKeyFactory propertyKeyFactory;

	/**
	 * Delegate {@link StatelessValueLoader}.
	 */
	private StatelessValueLoader valueLoader;

	/**
	 * Initiate.
	 * 
	 * @param propertyName
	 *            Property name.
	 * @param methodName
	 *            {@link Method} name.
	 * @param objectType
	 *            Object type.
	 * @param objectInstantiator
	 *            {@link ObjectInstantiator}.
	 * @param propertyKeyFactory
	 *            {@link PropertyKeyFactory}.
	 */
	public ObjectParameterValueLoaderFactory(String propertyName, String methodName, Class<?> objectType,
			ObjectInstantiator objectInstantiator, PropertyKeyFactory propertyKeyFactory) {
		this.propertyName = propertyName;
		this.methodName = methodName;
		this.objectType = objectType;
		this.objectInstantiator = objectInstantiator;
		this.propertyKeyFactory = propertyKeyFactory;
	}

	/**
	 * Specifies the {@link StatelessValueLoader}.
	 * 
	 * @param valueLoader
	 *            {@link StatelessValueLoader}.
	 */
	public void setValueLoader(StatelessValueLoader valueLoader) {
		this.valueLoader = valueLoader;
	}

	/*
	 * ========================= ValueLoaderFactory =====================
	 */

	@Override
	public String getPropertyName() {
		return this.propertyName;
	}

	@Override
	public StatelessValueLoader createValueLoader(Class<?> clazz) throws Exception {

		// Obtain the loader method
		final Method loaderMethod = clazz.getMethod(this.methodName, this.objectType);

		// Return the value loader
		return new StatelessValueLoader() {
			@Override
			public void loadValue(Object object, String name, int nameIndex, String value, HttpValueLocation location,
					Map<PropertyKey, Object> state) throws HttpException {

				// Determine parameter key (-1 to ignore separator '.')
				String propertyName = name.substring(0, nameIndex - 1);
				PropertyKey key = ObjectParameterValueLoaderFactory.this.propertyKeyFactory
						.createPropertyKey(propertyName);

				// Load the parameter only once
				Object parameter = state.get(key);
				if (parameter == null) {
					// Instantiate the parameter object
					try {
						parameter = ObjectParameterValueLoaderFactory.this.objectInstantiator
								.instantiate(ObjectParameterValueLoaderFactory.this.objectType);
					} catch (Exception ex) {
						if (ex instanceof HttpException) {
							throw (HttpException) ex;
						} else {
							throw new HttpException(ex);
						}
					}

					// Record on state for possible further loading
					state.put(key, parameter);

					// Load the parameter
					ValueLoaderSource.loadValue(object, loaderMethod, parameter);
				}

				// Load the remaining object
				ObjectParameterValueLoaderFactory.this.valueLoader.loadValue(parameter, name, nameIndex, value,
						location, state);
			}
		};
	}

}