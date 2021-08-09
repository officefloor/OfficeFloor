/*-
 * #%L
 * JDBC Persistence
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

package net.officefloor.jdbc.datasource;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.test.MockClockFactory;

/**
 * Default {@link DataSourceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultDataSourceFactory implements DataSourceFactory, ConnectionPoolDataSourceFactory {

	/**
	 * <p>
	 * Convenience method to create a {@link DataSource}.
	 * <p>
	 * This is typically used in testing.
	 * 
	 * @param propertiesFileName Name of the {@link Properties} file on the class
	 *                           path.
	 * @return {@link DataSource}.
	 * @throws Exception If fails to load the {@link DataSource}.
	 */
	public static DataSource createDataSource(String propertiesFileName) throws Exception {

		// Ensure properties name starts with / (so absolute on class path)
		if (!propertiesFileName.startsWith("/")) {
			propertiesFileName = "/" + propertiesFileName;
		}

		// Obtain the properties
		InputStream propertiesFile = DefaultDataSourceFactory.class.getResourceAsStream(propertiesFileName);
		if (propertiesFile == null) {
			throw new FileNotFoundException("Can not find file " + propertiesFileName + " on the class path");
		}
		Properties properties = new Properties();
		properties.load(propertiesFile);

		// Create the data source from the properties
		return createDataSource(properties);
	}

	/**
	 * <p>
	 * Convenience method to create a {@link DataSource}.
	 * <p>
	 * This is typically used in testing.
	 * 
	 * @param properties {@link Properties} to configure the {@link DataSource}.
	 * @return {@link DataSource}.
	 * @throws Exception If fails to load the {@link DataSource}.
	 */
	public static DataSource createDataSource(Properties properties) throws Exception {

		// Provide the source properties
		SourcePropertiesImpl sourceProperties = new SourcePropertiesImpl();
		for (String name : properties.stringPropertyNames()) {
			String value = properties.getProperty(name);
			sourceProperties.addProperty(name, value);
		}

		// Create the source context
		SourceContext rootContext = new SourceContextImpl("ROOT", false, null,
				DefaultDataSourceFactory.class.getClassLoader(), new MockClockFactory());
		SourceContext configuredContext = new SourceContextImpl("ROOT.DataSource", false, null, rootContext,
				new SourcePropertiesImpl(sourceProperties));

		// Create the data source
		return new DefaultDataSourceFactory().createDataSource(configuredContext);
	}

	/**
	 * {@link Property} name for the {@link DataSource} {@link Class}.
	 */
	public static final String PROPERTY_DATA_SOURCE_CLASS_NAME = "datasource.class";

	/**
	 * Loads the properties onto the {@link DataSource}.
	 * 
	 * @param <S>        {@link DataSource} type.
	 * @param dataSource {@link DataSource}.
	 * @param context    {@link SourceContext}.
	 * @throws Exception If fails to load properties.
	 */
	@SuppressWarnings("unchecked")
	public static <S> void loadProperties(S dataSource, SourceContext context) throws Exception {

		// Obtain the class
		Class<S> dataSourceClass = (Class<S>) dataSource.getClass();

		// Obtain the setters from the class
		List<Setter<S>> setters = new LinkedList<Setter<S>>();
		for (Method method : dataSource.getClass().getMethods()) {

			// Ensure the method is a public setter with only one argument
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}
			if (!method.getName().startsWith("set")) {
				continue;
			}
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 1) {
				continue;
			}

			// Create and add the setter
			Setter<S> setter = new Setter<>(dataSourceClass, method);
			setters.add(setter);
		}

		// Load the properties for the data source
		for (Setter<S> setter : setters) {

			// Obtain the property value
			String propertyName = setter.getPropertyName();
			String propertyValue = context.getProperty(propertyName, null);
			if ((propertyValue == null) || (propertyValue.trim().length() == 0)) {
				// Property not configured, so do not load
				continue;
			}

			// Load the property value
			setter.setPropertyValue(dataSource, propertyValue);
		}
	}

	/*
	 * ================== DataSourceFactory =========================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public DataSource createDataSource(SourceContext context) throws Exception {

		// Obtain the data source
		String dataSourceClassName = context.getProperty(PROPERTY_DATA_SOURCE_CLASS_NAME);
		Class<?> objectClass = context.loadClass(dataSourceClassName);
		if (!DataSource.class.isAssignableFrom(objectClass)) {
			throw new IllegalArgumentException(
					"Non " + DataSource.class.getSimpleName() + " class configured: " + dataSourceClassName);
		}
		Class<? extends DataSource> dataSourceClass = (Class<? extends DataSource>) objectClass;

		// Load the data source
		DataSource dataSource = dataSourceClass.getDeclaredConstructor().newInstance();

		// Load the properties
		loadProperties(dataSource, context);

		// Return the data source
		return dataSource;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ConnectionPoolDataSource createConnectionPoolDataSource(SourceContext context) throws Exception {

		// Obtain the connection pool data source
		String connectionPoolDataSourceClassName = context.getProperty(PROPERTY_DATA_SOURCE_CLASS_NAME);
		Class<?> objectClass = context.loadClass(connectionPoolDataSourceClassName);
		if (!ConnectionPoolDataSource.class.isAssignableFrom(objectClass)) {
			throw new IllegalArgumentException("Non " + ConnectionPoolDataSource.class.getSimpleName()
					+ " class configured: " + connectionPoolDataSourceClassName);
		}
		Class<? extends ConnectionPoolDataSource> dataSourceClass = (Class<? extends ConnectionPoolDataSource>) objectClass;

		// Load the data source
		ConnectionPoolDataSource dataSource = dataSourceClass.getDeclaredConstructor().newInstance();

		// Load the properties
		loadProperties(dataSource, context);

		// Return the data source
		return dataSource;
	}

	/**
	 * Setter {@link Method} wrapper.
	 */
	private static class Setter<S> {

		/**
		 * Class containing this.
		 */
		private final Class<S> clazz;

		/**
		 * Setter method.
		 */
		private final Method method;

		/**
		 * Initiate.
		 * 
		 * @param clazz  Class for the {@link Method}.
		 * @param method Setter method.
		 */
		private Setter(Class<S> clazz, Method method) {
			this.clazz = clazz;
			this.method = method;
		}

		/**
		 * Obtains the property name for this setter.
		 * 
		 * @return Property name for this setter.
		 */
		private String getPropertyName() {
			String propertyName = this.method.getName();
			propertyName = propertyName.substring("set".length());
			propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
			return propertyName;
		}

		/**
		 * Sets the value onto the bean.
		 * 
		 * @param dataSource {@link DataSource} to have the value loaded.
		 * @param value      Value to set on the bean.
		 * @throws Exception If fails to set the value.
		 */
		private void setValue(S dataSource, Object value) throws Exception {

			// Obtain the method to load
			Method setMethod;
			if (dataSource.getClass() == this.clazz) {
				setMethod = this.method;
			} else {
				// Obtain method from sub type
				setMethod = dataSource.getClass().getMethod(this.method.getName(), this.method.getParameterTypes());
			}

			try {
				// Set the value onto the object
				setMethod.invoke(dataSource, value);
			} catch (InvocationTargetException ex) {
				// Throw bean failure
				Throwable cause = ex.getCause();
				if (cause instanceof Exception) {
					throw (Exception) cause;
				} else if (cause instanceof Error) {
					throw (Error) cause;
				} else {
					// Can not throw, so indicate via invocation failure
					throw ex;
				}
			}
		}

		/**
		 * Sets the property value onto the bean.
		 * 
		 * @param dataSource {@link DataSource} to have the value loaded.
		 * @param value      Value to set on the bean.
		 * @throws Exception If fails to set property value.
		 */
		private void setPropertyValue(S dataSource, String value) throws Exception {

			// Transform the value to set on bean
			Object loadValue;
			Class<?> parameterType = this.method.getParameterTypes()[0];
			if (String.class.isAssignableFrom(parameterType)) {
				loadValue = value;
			} else if (Integer.class.isAssignableFrom(parameterType) || int.class.isAssignableFrom(parameterType)) {
				loadValue = Integer.valueOf(value);
			} else if (Boolean.class.isAssignableFrom(parameterType) || boolean.class.isAssignableFrom(parameterType)) {
				loadValue = Boolean.valueOf(value);
			} else {
				// Unknown property type, so can not load
				throw new IllegalArgumentException("Unknown property value type " + parameterType.getName());
			}

			// Set the property value on the bean
			this.setValue(dataSource, loadValue);
		}
	}

}
