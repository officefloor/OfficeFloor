package net.officefloor.compile.internal.structure;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;

/**
 * {@link Node} to provide override {@link Property}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OverrideProperties {

	/**
	 * Obtains the override {@link PropertyList}.
	 * 
	 * @return Override {@link PropertyList}.
	 */
	PropertyList getOverridePropertyList();

}