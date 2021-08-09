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

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * Meta-data of the
 * {@link net.officefloor.plugin.xml.unmarshall.tree.XmlContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class XmlMappingMetaData {

	/**
	 * Identifies this XML mapping for reuse (or if already configured to use
	 * the same configuration).
	 */
	protected String id = null;

	/**
	 * Name of XML element/attribute which is the focus of this mapping.
	 */
	protected String elementName = null;

	/**
	 * Name of method to load value/object to target object.
	 */
	protected String loadMethodName = null;

	/**
	 * Should this be an object loading mapping, then this should be specified
	 * and be the fully quanlified name of the class of the object to load.
	 */
	protected String loadObjectClassName = null;

	/**
	 * Format for loading a value.
	 */
	protected String format = null;

	/**
	 * Static value to load to target object.
	 */
	protected String staticValue = null;

	/**
	 * The mappings of the loaded object, should the
	 * {@link #loadObjectClassName} be specified.
	 */
	protected List<XmlMappingMetaData> loadObjectConfiguration = null;

	/**
	 * Type of this mapping.
	 */
	protected XmlMappingType type = null;

	/**
	 * Default constructor to allow the {@link TreeXmlUnmarshaller} to configure
	 * itself.
	 */
	public XmlMappingMetaData() {
	}

	/**
	 * Convenience constructor for configuring the root of the mapping tree.
	 * 
	 * @param targetObjectType
	 *            Class of the target object.
	 * @param rootElementName
	 *            Root XML element name.
	 * @param targetObjectConfiguration
	 *            Configuration of the target object.
	 */
	public XmlMappingMetaData(Class<?> targetObjectType,
			String rootElementName,
			XmlMappingMetaData[] targetObjectConfiguration) {
		// Specify state
		this.type = XmlMappingType.ROOT;
		this.elementName = rootElementName;
		this.loadObjectClassName = targetObjectType.getName();
		this.setLoadObjectConfiguration(targetObjectConfiguration);
	}

	/**
	 * Convenience constructor for configuring to load a value.
	 * 
	 * @param elementName
	 *            XML element/attribute name containing the value.
	 * @param loadMethodName
	 *            Method by which to load the value onto the target object.
	 * @param format
	 *            Format of the value if required otherwise make pass
	 *            <code>null</code>.
	 */
	public XmlMappingMetaData(String elementName, String loadMethodName,
			String format) {
		// Specify state
		this.type = XmlMappingType.VALUE;
		this.elementName = elementName;
		this.loadMethodName = loadMethodName;
		this.format = format;
	}

	/**
	 * Convenience constructor for configuring to load an object.
	 * 
	 * @param elementName
	 *            XML element name.
	 * @param loadMethodName
	 *            Method by which to load the object onto the target object.
	 * @param loadObjectClass
	 *            Class of the object to load onto the target object.
	 * @param loadObjectConfiguration
	 *            Configuration of load object.
	 * @param id
	 *            Identifier of the XML mapping. If not be re-used may be
	 *            <code>null</code>.
	 */
	public XmlMappingMetaData(String elementName, String loadMethodName,
			Class<?> loadObjectClass,
			XmlMappingMetaData[] loadObjectConfiguration, String id) {
		// Specify state
		this.type = XmlMappingType.OBJECT;
		this.elementName = elementName;
		this.loadMethodName = loadMethodName;
		this.loadObjectClassName = loadObjectClass.getName();
		this.id = id;

		// Specify the load object configuration
		this.setLoadObjectConfiguration(loadObjectConfiguration);
	}

	/**
	 * Convenience constructor for configuring to load by reference.
	 * 
	 * @param loadMethodName
	 *            Method by which to begin reference loading to target object.
	 * @param id
	 *            Identifier of the XML mapping to re-use.
	 */
	public XmlMappingMetaData(String loadMethodName, String id) {
		// Store state
		this.type = XmlMappingType.REFERENCE;
		this.loadMethodName = loadMethodName;
		this.id = id;
	}

	/**
	 * Convenience constructor for configuring to load a static reference.
	 * 
	 * @param type
	 *            Type of this XML mapping.
	 * @param loadMethodName
	 *            Method by which to load the static value to target object.
	 * @param staticValue
	 *            Static value to load to target object.
	 */
	public XmlMappingMetaData(XmlMappingType type, String loadMethodName,
			String staticValue) {
		// Store state
		this.type = XmlMappingType.STATIC;
		this.loadMethodName = loadMethodName;
		this.staticValue = staticValue;
	}

	/**
	 * Obtains the type of this mapping.
	 * 
	 * @return Type of this mapping.
	 * @throws XmlMarshallException
	 *             If no type has been specified.
	 */
	public XmlMappingType getType() throws XmlMarshallException {
		// Ensure have type
		if (this.type == null) {
			throw new XmlMarshallException("Type must be specified.");
		}
		return this.type;
	}

	/**
	 * Enables specifying the type name.
	 * 
	 * @param typeName
	 *            Name of the type.
	 */
	public void setType(String typeName) {
		this.type = XmlMappingType.valueOf(typeName);
	}

	/**
	 * Obtains the identifier of this XML mapping.
	 * 
	 * @return Identifier of XML mapping.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Specifies the identifier of the XML mapping.
	 * 
	 * @param id
	 *            Identifier of the XML mapping.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Obtains the XML element/attribute name.
	 * 
	 * @return XML element/attribute name.
	 */
	public String getElementName() {
		return this.elementName;
	}

	/**
	 * Specifies the XML element/attribute name.
	 * 
	 * @param elementName
	 *            XML element/attribute name.
	 */
	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	/**
	 * Obtains the name of the method on the target object to load the
	 * value/object.
	 * 
	 * @return Load method name.
	 */
	public String getLoadMethodName() {
		return this.loadMethodName;
	}

	/**
	 * Specifies the name of the method on the target object to load the
	 * value/object.
	 * 
	 * @param loadMethodName
	 *            Load method name.
	 */
	public void setLoadMethodName(String loadMethodName) {
		this.loadMethodName = loadMethodName;
	}

	/**
	 * Obtains the fully qualified name of the class of the object to be loaded
	 * to the target object.
	 * 
	 * @return Load object class name.
	 */
	public String getLoadObjectClassName() {
		return this.loadObjectClassName;
	}

	/**
	 * Specifies the fully qualified name of the class of the object to be
	 * loaded to the target object.
	 * 
	 * @param loadObjectClassName
	 *            Load object class name.
	 */
	public void setLoadObjectClassName(String loadObjectClassName) {
		this.loadObjectClassName = loadObjectClassName;
	}

	/**
	 * Obtains the format for loading the value.
	 * 
	 * @return Format for loading the value.
	 */
	public String getFormat() {
		return this.format;
	}

	/**
	 * Specifies the format for loading the value.
	 * 
	 * @param format
	 *            Format for loading the value.
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * Obtains the static value to load to target object.
	 * 
	 * @return Static value.
	 */
	public String getStaticValue() {
		return this.staticValue;
	}

	/**
	 * Specifies the static value to load to target object.
	 * 
	 * @param staticValue
	 *            Static value.
	 */
	public void setStaticValue(String staticValue) {
		this.staticValue = staticValue;
	}

	/**
	 * Obtains the configuration for the object to load onto the target object.
	 * 
	 * @return Load object configuration.
	 */
	public XmlMappingMetaData[] getLoadObjectConfiguration() {
		return (this.loadObjectConfiguration == null ? new XmlMappingMetaData[0]
				: loadObjectConfiguration.toArray(new XmlMappingMetaData[0]));
	}

	/**
	 * Adds another {@link XmlMappingMetaData} configuration for the object to
	 * load onto the target object.
	 * 
	 * @param loadObjectMapping
	 *            {@link XmlMappingMetaData} for the object to load onto the
	 *            target object.
	 */
	public void addLoadObjectConfiguration(XmlMappingMetaData loadObjectMapping) {

		// Lazy load
		if (this.loadObjectConfiguration == null) {
			this.loadObjectConfiguration = new ArrayList<XmlMappingMetaData>();
		}

		// Add the load object mapping
		this.loadObjectConfiguration.add(loadObjectMapping);
	}

	/**
	 * Sets the {@link XmlMappingMetaData} configuration for the current object.
	 * 
	 * @param loadObjectConfiguration
	 *            {@link XmlMappingMetaData} configuration for the current
	 *            object.
	 */
	private void setLoadObjectConfiguration(
			XmlMappingMetaData[] loadObjectConfiguration) {
		// Specify the load object configuration
		this.loadObjectConfiguration = new ArrayList<XmlMappingMetaData>();
		if (loadObjectConfiguration != null) {
			for (XmlMappingMetaData mapping : loadObjectConfiguration) {
				this.loadObjectConfiguration.add(mapping);
			}
		}
	}

}
