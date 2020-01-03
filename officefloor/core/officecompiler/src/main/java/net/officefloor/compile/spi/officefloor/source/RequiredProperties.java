package net.officefloor.compile.spi.officefloor.source;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Enables the {@link OfficeFloorSource} to specify any required
 * {@link Property} instances necessary for loading the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RequiredProperties {

	/**
	 * Adds a required {@link Property}.
	 * 
	 * @param name
	 *            Name of the {@link Property} which is also used as its label.
	 */
	void addRequiredProperty(String name);

	/**
	 * Adds a required {@link Property}.
	 * 
	 * @param name
	 *            Name of the {@link Property}.
	 * @param label
	 *            Descriptive label for the {@link Property}.
	 */
	void addRequiredProperty(String name, String label);

}