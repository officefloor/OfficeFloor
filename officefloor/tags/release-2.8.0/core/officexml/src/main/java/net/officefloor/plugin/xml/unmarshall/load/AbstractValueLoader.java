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
	 * 
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
			throw new XmlMarshallException("Invoked load method failed.", ex
					.getCause());
		}
	}

}
