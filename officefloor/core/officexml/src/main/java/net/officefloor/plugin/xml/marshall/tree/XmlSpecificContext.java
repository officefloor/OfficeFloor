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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;
import net.officefloor.plugin.xml.marshall.translate.Translator;
import net.officefloor.plugin.xml.marshall.translate.TranslatorRegistry;

/**
 * Instance of {@link net.officefloor.plugin.xml.marshall.tree.XmlContext} for a
 * particular concrete class.
 * 
 * @author Daniel Sagenschneider
 */
public class XmlSpecificContext {

	/**
	 * Mappings to be applied for this context.
	 */
	protected final List<XmlMapping> mappings = new LinkedList<XmlMapping>();

	/**
	 * Initiate context from the upper bound type of the object.
	 * 
	 * @param upperBoundType
	 *            Upper bound type of the source object.
	 * @param contextElementName
	 *            Name of element or <code>null</code> if not output element for
	 *            context.
	 * @param configuration
	 *            Configuration of the context.
	 * @param context
	 *            Generic context to load child contexts.
	 * @param translatorRegistry
	 *            Registry of {@link Translator} to translate values.
	 * @param referenceRegistry
	 *            Registry of {@link XmlMapping} that may be referenced.
	 * @throws XmlMarshallException
	 *             If fails to configure this context.
	 */
	public XmlSpecificContext(Class<?> upperBoundType,
			String contextElementName, XmlMappingMetaData[] configuration,
			XmlContext context, TranslatorRegistry translatorRegistry,
			ReferencedXmlMappingRegistry referenceRegistry)
			throws XmlMarshallException {

		// Load the context element if specified
		if (contextElementName != null) {
			this.addXmlMapping(new StaticXmlMapping("<" + contextElementName));
		}

		// Flags to indicate movement from attribute mapping to element mapping
		boolean isLoadingAttributes = true;

		// Iterate over the configurations loading them
		if (configuration != null) {
			int mappingIndex = 0;
			for (XmlMappingMetaData mappingMetaData : configuration) {

				// Keep track of mapping index for child contexts
				mappingIndex++;

				// Ensure mapping has type
				XmlMappingType type = mappingMetaData.getType();
				if (type == null) {
					throw new XmlMarshallException("Mapping must specify type");
				}

				// Check if attribute mappings
				if (XmlMappingType.ATTRIBUTES.equals(type)) {

					// Ensure still loading attributes
					if (!isLoadingAttributes) {
						throw new XmlMarshallException(
								"Attempted to load attributes after elements have been loaded");
					}

					// Load the attributes
					this.loadAttributes(upperBoundType,
							mappingMetaData.getObjectMappings(),
							translatorRegistry);

				} else {

					// Element

					// Check if require stop attribute loading
					if (isLoadingAttributes) {
						// End context element if specified
						if (contextElementName != null) {
							this.addXmlMapping(new StaticXmlMapping(">"));
						}
						isLoadingAttributes = false;
					}

					// Obtain element name
					String elementName = mappingMetaData.getElementName();
					if (elementName != null) {
						elementName = elementName.trim();
					}

					// Obtain get method
					String getMethodName = mappingMetaData.getGetMethodName();
					Method getMethod = null;
					Class<?> returnType = null;
					if (getMethodName != null) {
						// Specify get method and its return type
						getMethod = XmlMarshallerUtil.obtainMethod(
								upperBoundType, getMethodName);
						returnType = getMethod.getReturnType();
					}

					// Obtain child mappings for object or item mappings for
					// collection
					XmlMappingMetaData[] objectMappings = mappingMetaData
							.getObjectMappings();

					// Create reference if necessary
					String id = mappingMetaData.getId();
					ProxyXmlMapping proxyXmlMapping = null;
					if (id != null) {
						switch (type) {
						case REFERENCE:
							// If reference do not proxy as will reference
							break;
						default:
							// Not reference therefore make possibly to
							// reference
							proxyXmlMapping = new ProxyXmlMapping();
							referenceRegistry.registerXmlMapping(id,
									proxyXmlMapping);

							break;
						}
					}

					// Create the XML mapping
					XmlMapping xmlMapping;
					switch (type) {
					case REFERENCE:
						// Obtain mapping to be referenced
						XmlMapping referencedXmlMapping = referenceRegistry
								.getXmlMapping(id);

						// Ensure have referenced xml mapping
						if (referencedXmlMapping == null) {
							throw new XmlMarshallException(
									"Can not find XML mapping by id '" + id
											+ "' to reference");
						}

						// Reference XML Mapping
						xmlMapping = new ReferenceXmlMapping(getMethod,
								referencedXmlMapping);

						break;

					case VALUE:
						// Check if static value
						String staticValue = mappingMetaData.getStaticValue();
						if (staticValue != null) {
							// Static value
							xmlMapping = new StaticXmlMapping("<" + elementName
									+ ">" + staticValue + "</" + elementName
									+ ">");
						} else {
							// Dynamic Value

							// Obtain the translator for the return value
							Translator translator = translatorRegistry
									.getTranslator(returnType);

							// Obtain whether to use raw value
							boolean isUseRaw = mappingMetaData.isUseRaw();

							// Dynamic value
							xmlMapping = new ValueXmlMapping(elementName,
									getMethod, translator, isUseRaw);
						}
						break;

					case OBJECT:
						// Object XML mapping
						xmlMapping = new ObjectXmlMapping(elementName,
								getMethod, returnType, objectMappings,
								String.valueOf(mappingIndex), context,
								translatorRegistry, referenceRegistry);
						break;

					case TYPE:
						// Type XML mapping
						xmlMapping = new TypeXmlMapping(elementName, getMethod,
								objectMappings, translatorRegistry,
								referenceRegistry);

						break;

					case COLLECTION:
						// Ensure return type is collection
						if (!Collection.class.isAssignableFrom(returnType)) {
							throw new XmlMarshallException(
									"Return type to be handled as collection is not a Collection but of type "
											+ returnType.getName());
						}

						// Collection XML mapping
						xmlMapping = new CollectionXmlMapping(elementName,
								getMethod, objectMappings, translatorRegistry,
								referenceRegistry);
						break;

					case ITEM:
						// Item should never be on its own
						throw new XmlMarshallException("Type "
								+ XmlMappingType.ITEM
								+ " must be direct child of either "
								+ XmlMappingType.TYPE + " or "
								+ XmlMappingType.COLLECTION);

					default:
						// Invalid type at this point
						throw new XmlMarshallException("Invalid type "
								+ type.toString() + " for specific cotext");
					}

					// Add the XML mapping
					if (proxyXmlMapping != null) {
						// Proxy therefore set mapping delegate
						proxyXmlMapping.setDelegate(xmlMapping);

						// Add the proxy for mapping
						this.addXmlMapping(proxyXmlMapping);
					} else {
						// Not referenced, thus add mapping directly
						this.addXmlMapping(xmlMapping);
					}
				}
			}
		}

		// Close context element if specified
		if (contextElementName != null) {
			// Close context element
			if (isLoadingAttributes) {
				// Only opening of context element
				this.addXmlMapping(new StaticXmlMapping("/>"));
			} else {
				// Close context after elements
				this.addXmlMapping(new StaticXmlMapping("</"
						+ contextElementName + ">"));
			}
		}
	}

	/**
	 * Initiate context to specific concrete implementation of the object.
	 * 
	 * @param source
	 *            The first instance of the object.
	 * @param sourceType
	 *            {@link Class} of the object.
	 * @param elementName
	 *            Name of element or <code>null</code> if not output element for
	 *            context.
	 * @param configuration
	 *            Configuration of the context.
	 * @param context
	 *            Generic context to obtain child contexts.
	 * @param translatorRegistry
	 *            Registry of {@link Translator} instances.
	 * @param referenceRegistry
	 *            Registry {@link XmlMapping} that may be referenced.
	 * @throws XmlMarshallException
	 *             If fails to configure this context.
	 */
	public XmlSpecificContext(Object source, Class<?> sourceType,
			String elementName, XmlMappingMetaData[] configuration,
			XmlContext context, TranslatorRegistry translatorRegistry,
			ReferencedXmlMappingRegistry referenceRegistry)
			throws XmlMarshallException {
		this(sourceType, elementName, configuration, context,
				translatorRegistry, referenceRegistry);
	}

	/**
	 * Does the marshalling of the source object to the output.
	 * 
	 * @param source
	 *            Source object to marshall.
	 * @param output
	 *            Output to send XML.
	 * @throws XmlMarshallException
	 *             If fails to marshall source object.
	 */
	public void marshall(Object source, XmlOutput output)
			throws XmlMarshallException {
		// Iterate over mappings applying them
		for (XmlMapping mapping : mappings) {
			// map object
			mapping.map(source, output);
		}
	}

	/**
	 * Loads the attribute mappings for this context.
	 * 
	 * @param upperBoundType
	 *            Upper bound type of the source object.
	 * @param attributes
	 *            Configuration of the attributes.
	 * @param translatorRegistry
	 *            Registry of {@link Translator} instances.
	 * @throws XmlMarshallException
	 *             If fails to load attribute mappings.
	 */
	protected void loadAttributes(Class<?> upperBoundType,
			XmlMappingMetaData[] attributes,
			TranslatorRegistry translatorRegistry) throws XmlMarshallException {

		// Load attributes
		if (attributes != null) {
			for (XmlMappingMetaData attributeMetaData : attributes) {

				// Ensure attribute mapping has type
				XmlMappingType type = attributeMetaData.getType();
				if (type == null) {
					throw new XmlMarshallException("Mapping must specify type");
				}

				// Handle type of mapping
				switch (type) {
				case ATTRIBUTE:

					// Attribute and remove surrounding white spaces
					String attributeName = attributeMetaData.getAttributeName();
					attributeName = attributeName.trim();

					// Check if static attribute
					String staticValue = attributeMetaData.getStaticValue();
					if (staticValue != null) {
						// Attribute static mapping
						this.addXmlMapping(new StaticXmlMapping(attributeName
								+ "=\""
								+ XmlMarshallerUtil
										.transformValueForXml(staticValue)
								+ "\" "));

					} else {
						// Attribute dynamic mapping

						// Obtain get method
						String getMethodName = attributeMetaData
								.getGetMethodName();
						Method getMethod = XmlMarshallerUtil.obtainMethod(
								upperBoundType, getMethodName);

						// Obtain the return type
						Class<?> returnType = getMethod.getReturnType();

						// Obtain the translator for the return value
						Translator translator = translatorRegistry
								.getTranslator(returnType);

						// Obtain whether to use raw value
						boolean isUseRaw = attributeMetaData.isUseRaw();

						// Attribute dynamic mapping
						this.addXmlMapping(new AttributeXmlMapping(
								attributeName, getMethod, translator, isUseRaw));
					}
					break;

				default:
					throw new XmlMarshallException("Type " + type.toString()
							+ " is invalid attribute mapping");
				}
			}
		}
	}

	/**
	 * Adds a {@link XmlMapping} to the listing of {@link XmlMapping} instances
	 * for this context.
	 * 
	 * @param mapping
	 *            {@link XmlMapping} to add as next mapping.
	 */
	protected void addXmlMapping(XmlMapping mapping) {
		this.mappings.add(mapping);
	}

}
