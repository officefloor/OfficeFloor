/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.xml.unmarshall.tree;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.unmarshall.load.ObjectLoaderFactory;
import net.officefloor.plugin.xml.unmarshall.load.ValueLoaderFactory;
import net.officefloor.plugin.xml.unmarshall.translate.TranslatorRegistry;

/**
 * Context for XML unmarshalling to target object.
 * 
 * @author Daniel
 */
public class XmlContext {

	/**
	 * Mappings of XML element to appropriate {@link ElementXmlMapping}.
	 */
	protected final Map<String, ElementXmlMapping> mappings = new HashMap<String, ElementXmlMapping>();

	/**
	 * Constructor that should be used by external classes to instantiate this.
	 * 
	 * @param targetObjectType
	 *            Class of the target object.
	 * @param rootNode
	 *            Name of root node to begin XML unmarshalling.
	 * @param configuration
	 *            Configuration of this {@link XmlContext}.
	 * @param translatorRegistry
	 *            Registry of translators for value translation.
	 * @param referencedRegistry
	 *            Registry of referenced {@link XmlMapping} instances.
	 * @throws XmlMarshallException
	 *             Should this {@link XmlContext} fail to be configured.
	 */
	public XmlContext(Class<?> targetObjectType, String rootNode,
			XmlMappingMetaData[] configuration,
			TranslatorRegistry translatorRegistry,
			ReferencedXmlMappingRegistry referencedRegistry)
			throws XmlMarshallException {

		// Create the root element mapping
		ElementXmlMapping rootMapping = new ElementXmlMapping();

		// Register the root element mapping
		this.mappings.put(rootNode, rootMapping);

		// Create the context for the root target object
		XmlContext rootContext = new XmlContext(targetObjectType,
				configuration, translatorRegistry, rootNode, this,
				referencedRegistry);

		// Set the root mapping
		rootMapping.setElementXmlMapping(new RootXmlMapping(rootContext));
	}

	/**
	 * Initiate the {@link XmlContext} from configuration.
	 * 
	 * @param targetObjectType
	 *            Class of the target object.
	 * @param configuration
	 *            Configuration of this {@link XmlContext}.
	 * @param translatorRegistry
	 *            Registry of translators for value translation.
	 * @param outerElementName
	 *            Element name of outer context to enable mapping attributes
	 *            onto appropriate context.
	 * @param outerContext
	 *            Outer {@link XmlContext} to map attributes on.
	 * @param referencedRegistry
	 *            Registry of referenced {@link XmlMapping} instances.
	 * @throws XmlMarshallException
	 *             Should this {@link XmlContext} fail to be configured.
	 */
	protected XmlContext(Class<?> targetObjectType,
			XmlMappingMetaData[] configuration,
			TranslatorRegistry translatorRegistry, String outerElementName,
			XmlContext outerContext,
			ReferencedXmlMappingRegistry referencedRegistry)
			throws XmlMarshallException {

		// Create the loader factories for the target object
		ValueLoaderFactory valueLoaderFactory = new ValueLoaderFactory(
				translatorRegistry, targetObjectType);
		ObjectLoaderFactory objectLoaderFactory = new ObjectLoaderFactory(
				targetObjectType);

		// Configure this context
		for (XmlMappingMetaData mappingMetaData : configuration) {

			// Ensure have type for mapping
			XmlMappingType mappingType = mappingMetaData.getType();
			if (mappingType == null) {
				throw new XmlMarshallException("Mapping must specify its type");
			}

			// Load method name is always specified on the mapping meta-data
			String loadMethodName = mappingMetaData.getLoadMethodName();

			// Set the focus meta-data
			XmlMappingMetaData focusMetaData = mappingMetaData;

			// Mapping for the meta-data
			ElementXmlMapping mapping = null;

			// Determine whether this is a reference
			String id = focusMetaData.getId();

			// Load reference meta-data if reference
			switch (mappingType) {
			case REFERENCE:
				// Use the referenced meta-data of the recursive mapping
				focusMetaData = referencedRegistry.getXmlMappingMetaData(id);
				if (focusMetaData == null) {
					throw new XmlMarshallException("XML mapping by id '" + id
							+ "' can not be found for referenced mapping.");
				}

				// Reference mapping
				mapping = referencedRegistry.getElementXmlMapping(id,
						targetObjectType);
			}

			// Break node into element and attribute name (format:
			// <element>@<attribute>)
			String nodeName = focusMetaData.getElementName();
			String elementName = null;
			String attributeName = null;
			if (nodeName != null) {
				String[] nodePath = nodeName
						.split(TreeXmlUnmarshaller.ATTRIBUTE_SEPARATOR);
				elementName = nodePath[0];
				attributeName = (nodePath.length > 1 ? nodePath[1] : null);
			}

			// Check if have mapping by reference
			if (mapping != null) {

				// Register the referenced mapping
				this.mappings.put(elementName, mapping);

			} else {

				// No reference therefore must create mapping for element

				// Check if static mapping
				if (XmlMappingType.STATIC.equals(focusMetaData.getType())) {

					// Load static mapping onto outer element context always
					mapping = outerContext
							.getElementXmlMapping(outerElementName);

					// Obtain static value
					String staticValue = focusMetaData.getStaticValue();

					// Create the static value loader xml mapping
					XmlMapping xmlMapping = new StaticXmlMapping(
							valueLoaderFactory.createStaticValueLoader(
									loadMethodName, staticValue));

					// Static value mapping
					mapping.addStaticXmlMappings(xmlMapping);

				} else {

					// Value/Object mapping
					XmlMapping xmlMapping = null;

					// Obtain element mapping
					if ((attributeName != null)
							&& elementName.equals(outerElementName)) {
						// Attribute of outer element
						mapping = outerContext
								.getElementXmlMapping(elementName);
					}
					if (mapping == null) {
						// Check if matches element of current context
						mapping = this.mappings.get(elementName);
						if (mapping == null) {
							// No existing mapping, thus create the mapping
							mapping = new ElementXmlMapping();
							this.mappings.put(elementName, mapping);

							// Register the created element mapping if named
							if (id != null) {
								referencedRegistry.registerReferenceXmlMapping(
										id, targetObjectType, mapping,
										focusMetaData);
							}
						}
					}

					// Obtain Value/Object XML mapping
					switch (focusMetaData.getType()) {
					case VALUE:
						// Create the dynamic value loader xml mapping
						xmlMapping = new ValueXmlMapping(valueLoaderFactory
								.createDynamicValueLoader(loadMethodName));
						break;

					case OBJECT:
						// Obtain the class name of the load object
						String loadObjectClassName = focusMetaData
								.getLoadObjectClassName();

						// Ensure the load object class name specified
						if (loadObjectClassName == null) {
							throw new XmlMarshallException(
									"Must provide class attribute value for load method "
											+ loadMethodName + " [element "
											+ elementName + "]");
						}

						// Obtain the Class of the load object
						Class<?> loadedObjectType;
						try {
							loadedObjectType = Class
									.forName(loadObjectClassName);
						} catch (ClassNotFoundException ex) {
							// Propagate failure
							throw new XmlMarshallException(
									"Can not find load object class '"
											+ loadObjectClassName + "'", ex);
						}

						// Create the new xml context for the loaded object
						XmlContext loadObjectXmlContext = new XmlContext(
								loadedObjectType, focusMetaData
										.getLoadObjectConfiguration(),
								translatorRegistry, elementName, this,
								referencedRegistry);

						// Create the object xml mapping
						ObjectXmlMapping objectXmlMapping = new ObjectXmlMapping(
								objectLoaderFactory.createObjectLoader(
										loadMethodName, loadedObjectType),
								loadObjectXmlContext);

						// Set for adding
						xmlMapping = objectXmlMapping;

					}

					// Register Value/Object XML mapping
					if (attributeName == null) {
						// Element mapping
						mapping.setElementXmlMapping(xmlMapping);
					} else {
						// Attribute mapping
						mapping.addAttributeXmlMapping(attributeName,
								xmlMapping);
					}
				}
			}
		}
	}

	/**
	 * Obtains the {@link ElementXmlMapping} for the element.
	 * 
	 * @param elementName
	 *            Element name.
	 * @return {@link ElementXmlMapping} for element or <code>null</code> if
	 *         there is no mapping.
	 */
	public ElementXmlMapping getElementXmlMapping(String elementName) {
		return this.mappings.get(elementName);
	}

}