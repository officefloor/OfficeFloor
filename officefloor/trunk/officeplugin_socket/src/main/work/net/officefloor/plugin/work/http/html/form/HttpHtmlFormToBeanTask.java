/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.work.http.html.form;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.source.HttpStatus;
import net.officefloor.plugin.work.http.HttpException;

/**
 * Loads the HTML FORM parameter values onto the corresponding properties of the
 * bean.
 *
 * @author Daniel Sagenschneider
 */
public class HttpHtmlFormToBeanTask
		extends
		AbstractSingleTask<HttpHtmlFormToBeanTask, HttpHtmlFormToBeanTask.HttpHtmlFormToBeanTaskDependencies, None> {

	/**
	 * Dependencies for the {@link HttpHtmlFormToBeanTask}.
	 */
	public static enum HttpHtmlFormToBeanTaskDependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * Bean {@link Class}.
	 */
	private final Class<?> beanClass;

	/**
	 * {@link Method} instances by property name to load values onto the bean
	 * instance.
	 */
	private final Map<String, Method> beanProperties;

	/**
	 * Initiate.
	 *
	 * @param beanClass
	 *            Class of the bean to load with values from HTML FORM.
	 * @param aliasMappings
	 *            Alias mappings so HTML FORM parameter names need not match
	 *            exactly the bean property names.
	 * @throws Exception
	 *             If bean does not follow bean pattern and have a default
	 *             constructor.
	 */
	public HttpHtmlFormToBeanTask(Class<?> beanClass,
			Map<String, String> aliasMappings) throws Exception {
		this.beanClass = beanClass;

		// Ensure the bean has a public default constructor
		boolean hasPublicDefaultConstructor = false;
		for (Constructor<?> constructor : this.beanClass.getConstructors()) {

			// Ensure a public constructor
			if (!Modifier.isPublic(constructor.getModifiers())) {
				continue;
			}

			// Ensure no parameters (default constructor)
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			if ((parameterTypes != null) && (parameterTypes.length > 0)) {
				continue;
			}

			// Is a public default constructor
			hasPublicDefaultConstructor = true;
		}
		if (!hasPublicDefaultConstructor) {
			throw new Exception("Bean class '" + this.beanClass.getName()
					+ "' must have a public default constructor");
		}

		// Create the mapping of property to setter method
		this.beanProperties = new HashMap<String, Method>();
		for (Method method : this.beanClass.getMethods()) {

			// Ensure a public void method
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}
			if ((method.getReturnType() != null)
					&& (method.getReturnType() != Void.TYPE)) {
				continue;
			}

			// Ensure has only a String parameter
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 1) {
				continue;
			}
			Class<?> parameterType = parameterTypes[0];
			if (!String.class.isAssignableFrom(parameterType)) {
				continue;
			}

			// Ensure the method begins with 'set'
			final String SETTER_PREFIX = "set";
			String methodName = method.getName();
			if (!methodName.startsWith(SETTER_PREFIX)) {
				continue;
			}

			// Ensure there is a property name
			String propertyName = methodName.substring(SETTER_PREFIX.length(),
					methodName.length());
			if ((propertyName == null) || (propertyName.length() == 0)) {
				continue;
			}

			// Ensure first character of property name is lower case
			propertyName = propertyName.substring(0, 1).toLowerCase()
					+ propertyName.substring(1);

			// Load the property
			this.beanProperties.put(propertyName, method);

			// Load any aliases
			if (aliasMappings != null) {
				for (String alias : aliasMappings.keySet()) {
					String aliasPropertyName = aliasMappings.get(alias);
					if (propertyName.equals(aliasPropertyName)) {
						// Load the alias
						this.beanProperties.put(alias, method);
					}
				}
			}
		}
	}

	/*
	 * ==================== Task ===========================================
	 */

	@Override
	public Object doTask(
			TaskContext<HttpHtmlFormToBeanTask, HttpHtmlFormToBeanTaskDependencies, None> context)
			throws HttpException, BeanMapException {

		// Obtain the HTTP request
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(HttpHtmlFormToBeanTaskDependencies.SERVER_HTTP_CONNECTION);
		HttpRequest request = connection.getHttpRequest();

		// Obtain the HTTP method and path
		String method = request.getMethod().toUpperCase();
		String path = request.getRequestURI();

		// Obtain the body (expect body if POST so give greater capacity)
		CharacterBuffer httpBody = new CharacterBuffer(
				("POST".equals(method) ? 256 : 32));
		InputStream body = request.getBody().getInputStream();
		try {
			for (int value = body.read(); value != -1; value = body.read()) {
				httpBody.append((char) value);
			}
		} catch (IOException ex) {
			// Should have the body, so server failure
			throw new HttpException(HttpStatus._500, "Failure obtaining body",
					ex);
		}

		// Create the HTML form
		HtmlForm form;
		try {
			form = (httpBody.length() == 0 ? new HtmlForm(path) : new HtmlForm(
					path, httpBody.toString()));
		} catch (URISyntaxException ex) {
			// Indicate a HTTP failure
			throw new HttpException(HttpStatus._400, ex);
		}

		try {
			// Create the bean instance
			Object bean = this.beanClass.newInstance();

			// Load the parameters onto the bean
			for (HtmlFormParameter parameter : form.getParameters()) {

				// Obtain the method to load the value
				String propertyName = parameter.getName();
				Method loadMethod = this.beanProperties.get(propertyName);

				// Load the value onto the bean (if have loader)
				if (loadMethod != null) {
					String propertyValue = parameter.getValue();
					loadMethod.invoke(bean, propertyValue);
				}
			}

			// Return the bean
			return bean;

		} catch (InvocationTargetException ex) {
			// Indicate cause of load failure
			throw new BeanMapException(ex);
		} catch (Exception ex) {
			// Indicate failure mapping
			throw new BeanMapException(ex);
		}
	}

}