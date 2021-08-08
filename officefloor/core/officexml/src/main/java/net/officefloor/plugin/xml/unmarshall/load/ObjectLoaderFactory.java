/*-
 * #%L
 * OfficeXml
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

package net.officefloor.plugin.xml.unmarshall.load;

import java.lang.reflect.Method;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * Loads objects onto a target object.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectLoaderFactory {

	/**
	 * Class of the target object.
	 */
	protected final Class<?> targetObjectType;

	/**
	 * Initiate the Object Loader.
	 * 
	 * @param targetObjectType
	 *            Class of the target object.
	 */
	public ObjectLoaderFactory(Class<?> targetObjectType) {
		// Store state
		this.targetObjectType = targetObjectType;
	}

	/**
	 * Creates the {@link ObjectLoader} for the load method of the target
	 * object.
	 * 
	 * @param loadMethodName
	 *            {@link Method} name that loads the {@link Object}.
	 * @param loadObjectType
	 *            Type of {@link Object} being loaded.
	 * @return {@link ObjectLoader} to load the object to the target object.
	 * @throws XmlMarshallException
	 *             Should there be a failure to create the {@link ObjectLoader}.
	 */
	public ObjectLoader createObjectLoader(String loadMethodName,
			Class<?> loadObjectType) throws XmlMarshallException {

		// Obtain the method to load the object
		Method loadMethod = this.findMethod(this.targetObjectType,
				loadMethodName, loadObjectType);

		// Ensure method exists
		if (loadMethod == null) {
			throw new XmlMarshallException("Could not find method "
					+ loadMethodName + "(" + loadObjectType.getName()
					+ ") on class " + this.targetObjectType.getName());
		}

		// Return the object loader
		return new ObjectLoader(loadMethod, loadObjectType);
	}

	/**
	 * Obtains the method of the input class by the input method name.
	 * 
	 * @param objectClass
	 *            Class to identify the method from.
	 * @param methodName
	 *            Name of method to find.
	 * @param parameterClass
	 *            {@link Class} of parameter to method.
	 * @return Method of class.
	 * @throws XmlMarshallException
	 *             If failed to find method to load.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Method findMethod(Class objectClass, String methodName,
			Class parameterClass) throws XmlMarshallException {

		try {
			Method method = null;

			try {
				// Attempt to obtain method by input
				method = objectClass.getMethod(methodName,
						new Class[] { parameterClass });
			} catch (NoSuchMethodException e) {
				// Not found thus keep trying
			}

			// Attempt on interface of parameter
			if (method == null) {
				for (Class parameterInterface : parameterClass.getInterfaces()) {
					if (method == null) {
						method = findMethod(objectClass, methodName,
								parameterInterface);
					}
				}
			}

			// Attempt to find method from super class of parameter if available
			if (method == null) {
				Class parameterSuperClass = parameterClass.getSuperclass();
				if (parameterSuperClass != null) {
					method = findMethod(objectClass, methodName,
							parameterSuperClass);
				}
			}

			// Return identified method
			return method;

		} catch (SecurityException ex) {
			// Propagate failure
			throw new XmlMarshallException("Method " + methodName + "("
					+ parameterClass.getName()
					+ ") is not accessible on class " + objectClass.getName());

		}
	}
}
