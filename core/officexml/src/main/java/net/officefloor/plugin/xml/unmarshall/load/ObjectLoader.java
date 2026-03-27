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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * Loader to load objects onto the target object.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectLoader {

	/**
	 * Method to load object onto the target object.
	 */
	protected final Method loadMethod;

	/**
	 * Type of object to be loaded to the target object.
	 */
	protected final Class<?> loadObjectType;

	/**
	 * Initiate.
	 * 
	 * @param loadMethod     Method to load object onto the target object.
	 * @param loadObjectType Type of object to be loaded to the target object.
	 */
	public ObjectLoader(Method loadMethod, Class<?> loadObjectType) {
		// Store state
		this.loadMethod = loadMethod;
		this.loadObjectType = loadObjectType;
	}

	/**
	 * Loads the object onto the target object and returns the loaded object.
	 * 
	 * @param targetObject Target object to have object loaded onto it.
	 * @return Object created and loaded onto the target object.
	 * @throws XmlMarshallException If fails to marshal the data onto the
	 *                              {@link Object}.
	 */
	public Object loadObject(Object targetObject) throws XmlMarshallException {

		// Create object to load
		Object loadObject;
		try {
			loadObject = this.loadObjectType.getDeclaredConstructor().newInstance();
		} catch (IllegalAccessException ex) {
			// Propagate failure
			throw new XmlMarshallException("Illegal access to construction of '" + this.loadObjectType.getName() + "'",
					ex);
		} catch (NoSuchMethodException | InvocationTargetException | InstantiationException ex) {
			// Propagate failure
			throw new XmlMarshallException("Instantiation failure of '" + this.loadObjectType.getName() + "'",
					ex.getCause());
		}

		// Load the object
		try {
			// Load value onto target object
			this.loadMethod.invoke(targetObject, new Object[] { loadObject });
		} catch (IllegalArgumentException ex) {
			// Propagate failure
			throw new XmlMarshallException("Invalid parameter to method '" + this.loadMethod.getName()
					+ "' which was of type '" + this.loadObjectType.getName() + "'", ex);
		} catch (IllegalAccessException ex) {
			// Propagate failure
			throw new XmlMarshallException("Illegal access to method '" + this.loadMethod.getName() + "'", ex);
		} catch (InvocationTargetException ex) {
			// Propagate failure
			throw new XmlMarshallException("Invoked load method failed.", ex.getCause());
		}

		// Return load object
		return loadObject;
	}

}
