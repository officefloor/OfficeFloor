package net.officefloor.compile.officefloor;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceProperty;

/**
 * <code>Type definition</code> of a configurable {@link Property} of the
 * {@link ManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorManagedObjectSourcePropertyType extends
		ManagedObjectSourceProperty {

	/**
	 * Obtains the default value for this {@link Property}.
	 * 
	 * @return Default value for this {@link Property}.
	 */
	String getDefaultValue();

}