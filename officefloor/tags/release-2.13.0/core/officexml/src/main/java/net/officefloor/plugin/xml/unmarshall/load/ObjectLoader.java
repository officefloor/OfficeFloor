/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
	 * @param loadMethod
	 *            Method to load object onto the target object.
	 * @param loadObjectType
	 *            Type of object to be loaded to the target object.
	 */
	public ObjectLoader(Method loadMethod, Class<?> loadObjectType) {
		// Store state
		this.loadMethod = loadMethod;
		this.loadObjectType = loadObjectType;
	}

	/**
	 * Loads the object onto the target object and returns the loaded object.
	 * 
	 * @param targetObject
	 *            Target object to have object loaded onto it.
	 * @return Object created and loaded onto the target object.
	 */
	public Object loadObject(Object targetObject) throws XmlMarshallException {

		// Create object to load
		Object loadObject;
		try {
			loadObject = this.loadObjectType.newInstance();
		} catch (IllegalAccessException ex) {
			// Propagate failure
			throw new XmlMarshallException(
					"Illegal access to construction of '"
							+ this.loadObjectType.getName() + "'", ex);
		} catch (InstantiationException ex) {
			// Propagate failure
			throw new XmlMarshallException("Instantiation failure of '"
					+ this.loadObjectType.getName() + "'", ex.getCause());
		}

		// Load the object
		try {
			// Load value onto target object
			this.loadMethod.invoke(targetObject, new Object[] { loadObject });
		} catch (IllegalArgumentException ex) {
			// Propagate failure
			throw new XmlMarshallException("Invalid parameter to method '"
					+ this.loadMethod.getName() + "' which was of type '"
					+ this.loadObjectType.getName() + "'", ex);
		} catch (IllegalAccessException ex) {
			// Propagate failure
			throw new XmlMarshallException("Illegal access to method '"
					+ this.loadMethod.getName() + "'", ex);
		} catch (InvocationTargetException ex) {
			// Propagate failure
			throw new XmlMarshallException("Invoked load method failed.", ex
					.getCause());
		}

		// Return load object
		return loadObject;
	}

}
