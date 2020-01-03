package net.officefloor.gef.configurer;

import javafx.beans.property.Property;
import net.officefloor.compile.properties.PropertyList;

/**
 * Builder for configuring a {@link PropertyList}.
 * 
 * @author Daniel Sagenschneider
 */
public interface PropertiesBuilder<M> extends Builder<M, PropertyList, PropertiesBuilder<M>> {

	/**
	 * Configures listening on the specification {@link PropertyList}.
	 * 
	 * @param specification
	 *            Specification {@link PropertyList}.
	 * @return <code>this</code>.
	 */
	PropertiesBuilder<M> specification(Property<PropertyList> specification);

}