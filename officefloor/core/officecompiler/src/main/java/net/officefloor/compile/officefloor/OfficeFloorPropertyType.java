package net.officefloor.compile.officefloor;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * <code>Type definition</code> for a {@link Property} of the
 * {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorPropertyType {

	/**
	 * Obtains the name for the {@link Property}.
	 * 
	 * @return Name for the {@link Property}.
	 */
	String getName();

	/**
	 * Obtains the label to describe the {@link Property}.
	 * 
	 * @return Label to describe the {@link Property}.
	 */
	String getLabel();

	/**
	 * Obtains the default value for this {@link Property}.
	 * 
	 * @return Default value for this {@link Property}.
	 */
	String getDefaultValue();

}