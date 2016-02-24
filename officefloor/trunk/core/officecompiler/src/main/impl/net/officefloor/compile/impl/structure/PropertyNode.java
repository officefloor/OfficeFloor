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
package net.officefloor.compile.impl.structure;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourcePropertyType;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourcePropertyType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;

/**
 * Node identifying an {@link Property}.
 *
 * @author Daniel Sagenschneider
 */
public class PropertyNode implements OfficeFloorTeamSourcePropertyType,
		OfficeFloorManagedObjectSourcePropertyType {

	/**
	 * Constructs the {@link PropertyNode} instances for the input
	 * {@link PropertyList}.
	 * 
	 * @param properties
	 *            {@link PropertyList}.
	 * @return {@link PropertyNode} instances.
	 */
	public static PropertyNode[] constructPropertyNodes(PropertyList properties) {

		// Create the listing of properties
		List<PropertyNode> propertyNodes = new LinkedList<PropertyNode>();
		for (Property property : properties) {
			propertyNodes.add(new PropertyNode(property));
		}

		// Return the properties
		return propertyNodes.toArray(new PropertyNode[0]);
	}

	/**
	 * Name of {@link Property}.
	 */
	private final String name;

	/**
	 * Label for the {@link Property}.
	 */
	private final String label;

	/**
	 * Default value for the {@link Property}.
	 */
	private final String defaultValue;

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            Name of the {@link Property}.
	 * @param label
	 *            Label for the {@link Property}.
	 * @param defaultValue
	 *            Default value for the {@link Property}.
	 */
	public PropertyNode(String name, String label, String defaultValue) {
		this.name = name;
		this.label = label;
		this.defaultValue = defaultValue;
	}

	/**
	 * Instantiate from a {@link Property}.
	 * 
	 * @param property
	 *            {@link Property}.
	 */
	public PropertyNode(Property property) {
		this(property.getName(), property.getLabel(), property.getValue());
	}

	/*
	 * =============== OfficeFloorTeamSourcePropertyType =====================
	 */

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public String getDefaultValue() {
		return this.defaultValue;
	}

}