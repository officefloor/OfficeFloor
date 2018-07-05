/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.xml.marshall.tree;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlMarshaller;
import net.officefloor.plugin.xml.XmlOutput;
import net.officefloor.plugin.xml.marshall.translate.TranslatorRegistry;

/**
 * Implementation of {@link net.officefloor.plugin.xml.XmlMarshaller} that is
 * capable of marshalling a tree object graph.
 * 
 * @author Daniel Sagenschneider
 */
public class TreeXmlMarshaller implements XmlMarshaller, XmlMapping, XmlWriter {

	/**
	 * {@link XmlContext} to handle the root object.
	 */
	protected final XmlContext context;

	/**
	 * Initiate with details to marshall the object.
	 * 
	 * @param metaData
	 *            Meta-data of the mappings.
	 * @param translatorRegistry
	 *            Registry of
	 *            {@link net.officefloor.plugin.xml.marshall.translate.Translator}
	 *            instances for marshalling values.
	 * @param referenceRegistry
	 *            Registry {@link XmlMapping} that may be referenced.
	 * @throws XmlMarshallException
	 *             If fail to configure.
	 */
	public TreeXmlMarshaller(XmlMappingMetaData metaData,
			TranslatorRegistry translatorRegistry,
			ReferencedXmlMappingRegistry referenceRegistry)
			throws XmlMarshallException {

		// Register this mapping if necessary
		ProxyXmlMapping proxyXmlMapping = null;
		String id = metaData.getId();
		if (id != null) {
			proxyXmlMapping = new ProxyXmlMapping();
			referenceRegistry.registerXmlMapping(id, proxyXmlMapping);
		}

		// Create the context
		this.context = new XmlContext(XmlMarshallerUtil.obtainClass(metaData
				.getUpperBoundType()), metaData.getElementName(), metaData
				.getObjectMappings(), true, translatorRegistry,
				referenceRegistry);

		// Set as delegate if necessary
		if (proxyXmlMapping != null) {
			proxyXmlMapping.setDelegate(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.XmlMarshaller#marshall(java.lang.Object,
	 *      net.officefloor.plugin.xml.XmlOutput)
	 */
	public void marshall(Object source, XmlOutput output)
			throws XmlMarshallException {
		// Write the source object as XML
		this.writeXml(source, output);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.XmlMapping#map(java.lang.Object,
	 *      net.officefloor.plugin.xml.XmlOutput)
	 */
	public void map(Object object, XmlOutput output)
			throws XmlMarshallException {
		// Marshall object
		this.marshall(object, output);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.XmlMapping#getWriter()
	 */
	public XmlWriter getWriter() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.XmlWriter#writeXml(java.lang.Object,
	 *      net.officefloor.plugin.xml.XmlOutput)
	 */
	public void writeXml(Object object, XmlOutput output)
			throws XmlMarshallException {
		// Marshall the object
		this.context.marshall(object, output);
	}

}
