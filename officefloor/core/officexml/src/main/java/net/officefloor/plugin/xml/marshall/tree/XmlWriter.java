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

package net.officefloor.plugin.xml.marshall.tree;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;

/**
 * Writes the XML for an object.
 * 
 * @author Daniel Sagenschneider
 */
public interface XmlWriter {

	/**
	 * Writes the XML for the input object.
	 * 
	 * @param object
	 *            Object to have XML written for it.
	 * @param output
	 *            Output to write the XML.
	 * @throws XmlMarshallException
	 *             If fails to write the object into XML.
	 */
	void writeXml(Object object, XmlOutput output) throws XmlMarshallException;

}
