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
 * {@link net.officefloor.plugin.xml.unmarshall.tree.XmlMapping} that puts the
 * root object into context.
 * 
 * @author Daniel Sagenschneider
 */
public class RootXmlMapping implements XmlMapping {

	/**
	 * {@link XmlContext} of the root target object.
	 */
	protected final XmlContext rootObjectContext;

	/**
	 * Initiate with {@link XmlContext} for the root target object.
	 * 
	 * @param rootObjectContext
	 *            {@link XmlContext} for the root target object.
	 */
	public RootXmlMapping(XmlContext rootObjectContext) {
		// Store state
		this.rootObjectContext = rootObjectContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.unmarshall.tree.XmlMapping#startMapping(net.officefloor.plugin.xml.unmarshall.tree.XmlState,
	 *      java.lang.String)
	 */
	public void startMapping(XmlState state, String elementName)
			throws XmlMarshallException {
		// Push root context into scope
		state.pushContext(elementName, state.getCurrentTargetObject(),
				this.rootObjectContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.unmarshall.tree.XmlMapping#endMapping(net.officefloor.plugin.xml.unmarshall.tree.XmlState,
	 *      java.lang.String)
	 */
	public void endMapping(XmlState state, String value)
			throws XmlMarshallException {
		// Pop the context happens on closing tag of pushed element name
	}

}
