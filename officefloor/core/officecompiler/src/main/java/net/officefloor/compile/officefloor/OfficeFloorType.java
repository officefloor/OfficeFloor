package net.officefloor.compile.officefloor;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * <code>Type definition</code> of an {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorType {

	/**
	 * Obtains the {@link Property} instances to be configured for this
	 * {@link OfficeFloor}.
	 * 
	 * @return {@link Property} instances to be configured for this
	 *         {@link OfficeFloor}.
	 */
	OfficeFloorPropertyType[] getOfficeFloorPropertyTypes();

	/**
	 * Obtains the <code>type definitions</code> of the {@link ManagedObjectSource}
	 * instances that may be configured for the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloorManagedObjectSourceType} instances.
	 */
	OfficeFloorManagedObjectSourceType[] getOfficeFloorManagedObjectSourceTypes();

	/**
	 * Obtains the <code>type definitions</code> of the {@link TeamSource} instances
	 * that may be configured for the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloorTeamSourceType} instances.
	 */
	OfficeFloorTeamSourceType[] getOfficeFloorTeamSourceTypes();

}