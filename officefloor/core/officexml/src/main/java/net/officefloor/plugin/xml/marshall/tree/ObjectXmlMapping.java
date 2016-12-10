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
package net.officefloor.plugin.xml.marshall.tree;

import java.lang.reflect.Method;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;
import net.officefloor.plugin.xml.marshall.translate.TranslatorRegistry;

/**
 * Writes an object into XML.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectXmlMapping extends AbstractXmlMapping {

	/**
	 * Name of XML element for this mapping.
	 */
	protected final String elementName;

	/**
	 * {@link XmlContext} for marshalling the object.
	 */
	protected final XmlContext context;

	/**
	 * Initiate the object XML mapping.
	 * 
	 * @param elementName
	 *            Name of XML element for this mapping or <code>null</code> if
	 *            no element.
	 * @param getMethod
	 *            Method to obtain the object to map.
	 * @param upperType
	 *            Upper bound type for this mapping.
	 * @param configuration
	 *            Configuration for the object being mapped.
	 * @param contextReference
	 *            Reference by which to register the context as a child context.
	 * @param context
	 *            Current context to register the object's child context.
	 * @param referenceRegistry
	 *            Registry of {@link XmlMapping} that may be referenced.
	 * @param translatorRegistry
	 *            Registry of
	 *            {@link net.officefloor.plugin.xml.marshall.translate.Translator}
	 *            instances.
	 * @throws XmlMarshallException
	 *             If fails to configure.
	 */
	public ObjectXmlMapping(String elementName, Method getMethod,
			Class<?> upperType, XmlMappingMetaData[] configuration,
			String contextReference, XmlContext context,
			TranslatorRegistry translatorRegistry,
			ReferencedXmlMappingRegistry referenceRegistry)
			throws XmlMarshallException {

		super(getMethod);

		// Store state
		this.elementName = elementName;

		// Obtain the child context for object from the input current context
		XmlContext childContext = context.getChildContext(contextReference);
		if (childContext == null) {
			// Not found thus create and register
			childContext = new XmlContext(upperType, this.elementName,
					configuration, true, translatorRegistry, referenceRegistry);
			context.registerChildContext(contextReference, childContext);
		}

		// Specify the context for the object
		this.context = childContext;
	}

	@Override
	public void writeXml(Object object, XmlOutput output)
			throws XmlMarshallException {

		// Marshall the object
		this.context.marshall(object, output);
	}

}