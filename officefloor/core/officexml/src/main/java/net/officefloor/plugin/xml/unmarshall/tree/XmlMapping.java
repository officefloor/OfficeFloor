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

package net.officefloor.plugin.xml.unmarshall.tree;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * Mapping of XML element/attribute to either a value/new object on a target
 * object.
 * 
 * @author Daniel Sagenschneider
 */
public interface XmlMapping {

	/**
	 * Starts the load of the value/object to the target object based on the
	 * current context and state of unmarshalling.
	 * 
	 * @param state
	 *            Current state of XML unmarshalling.
	 * @param elementName
	 *            Name of element/attribute being mapped.
	 * @throws XmlMarshallException
	 *             If fail to load XML mapping.
	 */
	void startMapping(XmlState state, String elementName)
			throws XmlMarshallException;

	/**
	 * Ends the load of the value/object to the target object based on the
	 * current context and state of unmarshalling.
	 * 
	 * @param state
	 *            state of XML unmarshalling.
	 * @param value
	 *            Value of the element/attribute.
	 * @throws XmlMarshallException
	 *             If fail to load XML mapping.
	 */
	void endMapping(XmlState state, String value) throws XmlMarshallException;
}
