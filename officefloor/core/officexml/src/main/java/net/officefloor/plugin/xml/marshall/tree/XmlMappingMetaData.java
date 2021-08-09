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

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the meta-data of the XML mappings.
 * 
 * @author Daniel Sagenschneider
 */
public class XmlMappingMetaData {

	/**
	 * Upper bound type of the object that this mapping is sourcing from.
	 */
	protected String upperBoundType = null;

	/**
	 * Element name for this mapping.
	 */
	protected String elementName = null;

	/**
	 * Attribute name flagging this mapping, if set, to be an attribute.
	 */
	protected String attributeName = null;

	/**
	 * Name of method to object the value/object for this mapping.
	 */
	protected String getMethodName = null;

	/**
	 * Static value for the mapping.
	 */
	protected String staticValue = null;

	/**
	 * Indicates if to use the value raw.
	 */
	protected boolean isUseRaw = false;

	/**
	 * The mappings of the object in current context.
	 */
	protected List<XmlMappingMetaData> objectMappings = null;

	/**
	 * Type of this Xml Mapping.
	 */
	protected XmlMappingType type = null;

	/**
	 * Id for referencing.
	 */
	protected String id = null;

	/**
	 * Default constructor to allow the
	 * {@link net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshaller} to
	 * configure this.
	 */
	public XmlMappingMetaData() {
		super();
	}

	/**
	 * Convenience constructor for creating the root/item mapping configuration.
	 * 
	 * @param type
	 *            Either {@link XmlMappingType#ROOT} or
	 *            {@link XmlMappingType#ITEM}.
	 * @param elementName
	 *            Name of root element.
	 * @param upperType
	 *            Upper bound type of root object.
	 * @param configuration
	 *            Configuration of the root object.
	 * @param id
	 *            Id to reference. May be <code>null</code> if not referenced.
	 */
	public XmlMappingMetaData(XmlMappingType type, String elementName,
			Class<?> upperType, XmlMappingMetaData[] configuration, String id) {

		// Ensure type either Root or Item
		switch (type) {
		case ROOT:
		case ITEM:
			break;
		default:
			throw new IllegalArgumentException(
					"Type for convenience constructor of root/item must be either: "
							+ XmlMappingType.ROOT.toString() + " or "
							+ XmlMappingType.ITEM.toString());
		}

		// Store state
		this.type = type;
		this.elementName = elementName;
		this.upperBoundType = upperType.getName();
		this.loadObjectMappings(configuration);
		this.id = id;
	}

	/**
	 * Convenience constructor for creating attributes mapping.
	 * 
	 * @param type
	 *            {@link XmlMappingType#ATTRIBUTES}.
	 * @param attributes
	 *            Mapping for the attributes of element.
	 */
	public XmlMappingMetaData(XmlMappingType type,
			XmlMappingMetaData[] attributes) {

		// Ensure type is Attributes
		switch (type) {
		case ATTRIBUTES:
			break;
		default:
			throw new IllegalArgumentException(
					"Type for convenience constructor of attribute must be "
							+ XmlMappingType.ATTRIBUTES);
		}

		// Store state
		this.type = type;
		this.loadObjectMappings(attributes);
	}

	/**
	 * Convenience constructor for creating an attribute/value mapping.
	 * 
	 * @param type
	 *            Either {@link XmlMappingType#ATTRIBUTE} or
	 *            {@link XmlMappingType#VALUE}.
	 * @param tagName
	 *            Name of the attribute/element.
	 * @param getMethodName
	 *            Method to obtain the value.
	 * @param isUseRaw
	 *            Indicates if use the raw value from object.
	 */
	public XmlMappingMetaData(XmlMappingType type, String tagName,
			String getMethodName, boolean isUseRaw) {

		// Ensure type either Attribute or Value
		switch (type) {
		case ATTRIBUTE:
			this.attributeName = tagName;
			break;
		case VALUE:
			this.elementName = tagName;
			break;
		default:
			throw new IllegalArgumentException(
					"Type for convenience constructor of attribute/value must be either: "
							+ XmlMappingType.ATTRIBUTE.toString() + " or "
							+ XmlMappingType.VALUE.toString());
		}

		// Store remaining state
		this.type = type;
		this.getMethodName = getMethodName;
		this.isUseRaw = isUseRaw;
	}

	/**
	 * Convenience constructor for creating a collection/object mapping.
	 * 
	 * @param type
	 *            Either {@link XmlMappingType#COLLECTION},
	 *            {@link XmlMappingType#TYPE} or {@link XmlMappingType#OBJECT}.
	 * @param elementName
	 *            Name of element for context.
	 * @param getMethodName
	 *            Method to obtain object.
	 * @param objectMappings
	 *            Configuration of the mappings for the object.
	 * @param id
	 *            Id for referencing this.
	 */
	public XmlMappingMetaData(XmlMappingType type, String elementName,
			String getMethodName, XmlMappingMetaData[] objectMappings, String id) {

		// Ensure type either Collection or Object
		switch (type) {
		case COLLECTION:
		case TYPE:
		case OBJECT:
			break;
		default:
			throw new IllegalArgumentException(
					"Type for convenience constructor of collection/object must be either: "
							+ XmlMappingType.COLLECTION.toString() + " or "
							+ XmlMappingType.OBJECT.toString());
		}

		// Store state
		this.type = type;
		this.elementName = elementName;
		this.getMethodName = getMethodName;
		this.loadObjectMappings(objectMappings);
		this.id = id;
	}

	/**
	 * Convenience constructor for creating a reference mapping.
	 * 
	 * @param type
	 *            {@link XmlMappingType#REFERENCE}.
	 * @param getMethodName
	 *            Method to obtain object.
	 * @param id
	 *            Id of mapping to handle object.
	 */
	public XmlMappingMetaData(XmlMappingType type, String getMethodName,
			String id) {
		// Ensure type is Reference
		switch (type) {
		case REFERENCE:
			break;
		default:
			throw new IllegalArgumentException(
					"Type for convenience constructor of reference must be "
							+ XmlMappingType.REFERENCE);
		}

		// Store state
		this.type = type;
		this.getMethodName = getMethodName;
		this.id = id;
	}

	/**
	 * Obtains the elemenet name.
	 * 
	 * @return Name for the element.
	 */
	public String getElementName() {
		return this.elementName;
	}

	/**
	 * Specifies the element name.
	 * 
	 * @param elementName
	 *            Name for the element.
	 */
	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	/**
	 * Obtains the attribute name.
	 * 
	 * @return Name for the attribute.
	 */
	public String getAttributeName() {
		return this.attributeName;
	}

	/**
	 * Specifies the attribute name.
	 * 
	 * @param attributeName
	 *            Name for the attribute.
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	/**
	 * Obtains the meta-data of the mappings for the object in the current
	 * context.
	 * 
	 * @return Meta-data of the mappings for the object in the current context.
	 */
	public XmlMappingMetaData[] getObjectMappings() {
		// Lazy loaded
		if (this.objectMappings == null) {
			return null;
		} else {
			return this.objectMappings.toArray(new XmlMappingMetaData[0]);
		}
	}

	/**
	 * Adds a {@link XmlMappingMetaData} for the object in the current context.
	 * 
	 * @param mapping
	 *            Meta-data mapping for object in current context.
	 */
	public void addObjectMapping(XmlMappingMetaData mapping) {
		// Lazy load object mappings
		if (this.objectMappings == null) {
			this.objectMappings = new ArrayList<XmlMappingMetaData>();
		}

		// Add mapping
		this.objectMappings.add(mapping);
	}

	/**
	 * Obtains the class name of the upper bound type of the current object in
	 * the context.
	 * 
	 * @return Upper bound type of the current object in the context.
	 */
	public String getUpperBoundType() {
		return this.upperBoundType;
	}

	/**
	 * Specifies the class name of the upper bound type of the current object in
	 * the context.
	 * 
	 * @param type
	 *            Upper bound type of the current object in the context.
	 */
	public void setUpperBoundType(String type) {
		this.upperBoundType = type;
	}

	/**
	 * Obtains the name of the method to get the value from the object in the
	 * current context.
	 * 
	 * @return Name of method.
	 */
	public String getGetMethodName() {
		return this.getMethodName;
	}

	/**
	 * Specifies the name of the method to get the value from the object in the
	 * current context.
	 * 
	 * @param getMethodName
	 *            Name of method.
	 */
	public void setGetMethodName(String getMethodName) {
		this.getMethodName = getMethodName;
	}

	/**
	 * Obtains the static value for the element/attribute.
	 * 
	 * @return Static value for the element/attribute.
	 */
	public String getStaticValue() {
		return this.staticValue;
	}

	/**
	 * Indicates whether the value should be used raw or whether it should be
	 * transformed for XML (ie special characters substituted).
	 * 
	 * @return Flag indicate whether to use the raw value.
	 */
	public boolean isUseRaw() {
		return this.isUseRaw;
	}

	/**
	 * Specifies whether the value should be used raw or whether it should be
	 * transformed for XML (ie special characters subsituted).
	 * 
	 * @param isUseRaw
	 *            Flag indicating whether to use the raw value.
	 */
	public void setIsUseRaw(String isUseRaw) {
		this.isUseRaw = Boolean.valueOf(isUseRaw);
	}

	/**
	 * Obtains the type of this XML mapping.
	 * 
	 * @return Type of this XML mapping.
	 */
	public XmlMappingType getType() {
		return this.type;
	}

	/**
	 * Specifies the type of this XML mapping.
	 * 
	 * @param type
	 *            Type for this XML mapping.
	 */
	public void setType(String type) {
		this.type = XmlMappingType.valueOf(type);
	}

	/**
	 * Obtains the Id for reference mapping.
	 * 
	 * @return Id for referencing.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Specifies the Id for reference mapping.
	 * 
	 * @param id
	 *            Id for referencing.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Loads the object mappings.
	 * 
	 * @param objectMappings
	 *            Object mappings.
	 */
	protected void loadObjectMappings(XmlMappingMetaData[] objectMappings) {
		// Ensure have object mappings
		if (objectMappings == null) {
			return;
		}

		// Add the configuration
		for (XmlMappingMetaData mapping : objectMappings) {
			this.addObjectMapping(mapping);
		}
	}
}
