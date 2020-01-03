package net.officefloor.compile.impl.structure;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourcePropertyType;
import net.officefloor.compile.officefloor.OfficeFloorPropertyType;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourcePropertyType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;

/**
 * Node identifying an {@link Property}.
 *
 * @author Daniel Sagenschneider
 */
public class PropertyNode implements OfficeFloorPropertyType,
		OfficeFloorTeamSourcePropertyType,
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