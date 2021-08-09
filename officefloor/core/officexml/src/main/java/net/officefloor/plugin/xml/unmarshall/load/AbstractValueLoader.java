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
 * Abstract value loader.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractValueLoader {

	/**
	 * Method to load value onto the target object.
	 */
	protected final Method loadMethod;

	/**
	 * Initiate with ability to load value onto target object.
	 * 
	 * @param loadMethod
	 *            {@link Method} to load the value.
	 */
	public AbstractValueLoader(Method loadMethod) {
		// Store state
		this.loadMethod = loadMethod;
	}

	/**
	 * Sets the value onto the target object.
	 * 
	 * @param targetObject
	 *            Target object to have value loaded on.
	 * @param value
	 *            Value to load onto the target object.
	 * @throws XmlMarshallException
	 *             Failed to load value onto target object.
	 */
	protected void setValue(Object targetObject, Object value)
			throws XmlMarshallException {
		try {
			// Load value onto target object
			this.loadMethod.invoke(targetObject, new Object[] { value });
		} catch (IllegalArgumentException ex) {
			// Propagate failure
			throw new XmlMarshallException("Invalid parameter to method '"
					+ this.loadMethod.getName() + "' which was of type '"
					+ value.getClass().getName() + "'", ex);
		} catch (IllegalAccessException ex) {
			// Propagate failure
			throw new XmlMarshallException("Illegal access to method '"
					+ this.loadMethod.getName() + "'", ex);
		} catch (InvocationTargetException ex) {
			// Propagate failure
			throw new XmlMarshallException("Invoked load method failed.",
					ex.getCause());
		}
	}

}
