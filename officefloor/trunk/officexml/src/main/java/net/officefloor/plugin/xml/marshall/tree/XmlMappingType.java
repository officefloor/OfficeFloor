/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
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
