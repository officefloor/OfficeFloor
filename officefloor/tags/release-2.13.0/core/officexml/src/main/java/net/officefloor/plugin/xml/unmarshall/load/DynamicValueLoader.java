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

import java.lang.reflect.Method;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.unmarshall.translate.Translator;

/**
 * Loader to load value onto target object.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamicValueLoader extends AbstractValueLoader {

	/**
	 * Translator to translate the XML string value to type to load onto the
	 * object.
	 */
	protected final Translator translator;

	/**
	 * Initiate.
	 * 
	 * @param loadMethod
	 *            Method to use to load value onto the target object.
	 * @param translator
	 *            Translator to translate the XML string value to type to load
	 *            onto the object.
	 */
	public DynamicValueLoader(Method loadMethod, Translator translator) {
		// Store state
		super(loadMethod);
		this.translator = translator;
	}

	/**
	 * Loads the XML string value onto the target object.
	 * 
	 * @param targetObject
	 *            Target object to have value loaded on.
	 * @param value
	 *            Value to load onto the target object.
	 * @throws XmlMarshallException
	 *             Failed to load value onto target object.
	 */
	public void loadValue(Object targetObject, String value)
			throws XmlMarshallException {
		// Obtain the translate value
		Object translatedValue = this.translator.translate(value);

		// Set value on target object
		this.setValue(targetObject, translatedValue);
	}

}