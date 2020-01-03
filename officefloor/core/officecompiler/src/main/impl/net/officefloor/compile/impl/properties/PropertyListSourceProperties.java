package net.officefloor.compile.impl.properties;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;

/**
 * {@link SourceProperties} initialised from a {@link PropertyList}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyListSourceProperties extends SourcePropertiesImpl {

	/**
	 * Initiate with {@link Property} instances within the {@link PropertyList}.
	 * 
	 * @param properties
	 *            {@link PropertyList}.
	 */
	public PropertyListSourceProperties(PropertyList properties) {
		if (properties != null) {
			for (Property property : properties) {
				this.addProperty(property.getName(), property.getValue());
			}
		}
	}

}