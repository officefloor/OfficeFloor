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
 * Mapping of object to XML.
 * 
 * @author Daniel Sagenschneider
 */
public interface XmlMapping {

	/**
	 * Maps the object into XML.
	 * 
	 * @param object
	 *            Object to map into XML.
	 * @param output
	 *            Output to send the XML.
	 * @throws XmlMarshallException
	 *             If fails to map object into XML.
	 */
	void map(Object object, XmlOutput output) throws XmlMarshallException;

	/**
	 * Obtains the {@link XmlWriter} for this mapping.
	 * 
	 * @return {@link XmlWriter} for this mapping.
	 */
	XmlWriter getWriter();
}
