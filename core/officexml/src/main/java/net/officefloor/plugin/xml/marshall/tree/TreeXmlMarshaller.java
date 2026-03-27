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
