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

/**
 * Type of the
 * {@link net.officefloor.plugin.xml.marshall.tree.XmlMappingMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public enum XmlMappingType {

	/**
	 * Root mapping.
	 */
	ROOT,

	/**
	 * Indicates the attributes for an element.
	 */
	ATTRIBUTES,

	/**
	 * A particular attribute for an element.
	 */
	ATTRIBUTE,

	/**
	 * Value of an object to be contained in an element.
	 */
	VALUE,

	/**
	 * Specific object that parents other elements.
	 */
	OBJECT,

	/**
	 * Generic object that has mappings specific to its sub-type implementation.
	 */
	TYPE,

	/**
	 * Collection of objects.
	 */
	COLLECTION,

	/**
	 * Specifies the type of object within a {@link #TYPE} or
	 * {@link #COLLECTION}.
	 */
	ITEM,

	/**
	 * Enables referencing other mappings. Mainly useful for recursive mappings.
	 */
	REFERENCE
}
