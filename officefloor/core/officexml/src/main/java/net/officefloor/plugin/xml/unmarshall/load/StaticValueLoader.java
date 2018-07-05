/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.xml.unmarshall.load;

import java.lang.reflect.Method;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * Loader to load a static value onto target object.
 * 
 * @author Daniel Sagenschneider
 */
public class StaticValueLoader extends AbstractValueLoader {

	/**
	 * Static value to load onto target object.
	 */
	protected final Object value;

	/**
	 * Initiate with details to static load.
	 * 
	 * @param loadMethod
	 *            Method to load static value onto target object.
	 * @param value
	 *            Static value to load onto target object.
	 */
	public StaticValueLoader(Method loadMethod, Object value) {
		super(loadMethod);

		// Store state
		this.value = value;
	}

	/**
	 * Loads the static value onto the target object.
	 * 
	 * @param targetObject
	 *            Target object to receive the static value.
	 * @throws XmlMarshallException
	 *             If fails to load the static value to target object.
	 */
	public void loadValue(Object targetObject) throws XmlMarshallException {
		// Set value on target object
		this.setValue(targetObject, this.value);
	}

}
