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
