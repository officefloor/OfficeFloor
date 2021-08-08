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

package net.officefloor.plugin.xml.marshall.translate;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * Contract to translate an object value to an string value for XML.
 * 
 * @author Daniel Sagenschneider
 */
public interface Translator {

	/**
	 * Translates the object value into a string for XML.
	 * 
	 * @param object
	 *            Object to be translated.
	 * @return String for XML.
	 * @throws XmlMarshallException
	 *             If fails to translate object.
	 */
	String translate(Object object) throws XmlMarshallException;
}
