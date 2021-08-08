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
