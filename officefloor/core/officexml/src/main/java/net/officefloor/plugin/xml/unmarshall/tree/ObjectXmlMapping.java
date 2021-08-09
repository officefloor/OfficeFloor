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
import net.officefloor.plugin.xml.unmarshall.load.ObjectLoader;

/**
 * {@link net.officefloor.plugin.xml.unmarshall.tree.XmlMapping} that loads
 * object onto target object.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectXmlMapping implements XmlMapping {

	/**
	 * Loads the object onto the target object.
	 */
	protected final ObjectLoader loader;

	/**
	 * {@link XmlContext} of the object to be loaded onto the target object.
	 */
	protected final XmlContext loadObjectContext;

	/**
	 * Initiate with loader and {@link XmlContext} for the object loaded.
	 * 
	 * @param loader
	 *            Loads the object onto the target object.
	 * @param loadObjectContext
	 *            {@link XmlContext} for this {@link XmlMapping}.
	 */
	public ObjectXmlMapping(ObjectLoader loader, XmlContext loadObjectContext) {
		// Store state
		this.loader = loader;
		this.loadObjectContext = loadObjectContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.plugin.xml.tree.XmlMapping#startMapping(net.officefloor
	 * .plugin.xml.tree.XmlState, java.lang.String)
	 */
	public void startMapping(XmlState state, String elementName)
			throws XmlMarshallException {

		// Load object onto target object
		Object loadedObject = this.loader.loadObject(state
				.getCurrentTargetObject());

		// Push new context
		state.pushContext(elementName, loadedObject, this.loadObjectContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.plugin.xml.tree.XmlMapping#endMapping(net.officefloor
	 * .plugin.xml.tree.XmlState, java.lang.String)
	 */
	public void endMapping(XmlState state, String value)
			throws XmlMarshallException {
		// Pop the context happens on closing tag of pushed element name
	}

}
