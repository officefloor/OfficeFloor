package net.officefloor.compile.officefloor;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * <code>Type definition</code> of a {@link TeamSource} available to be
 * configured in the {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorTeamSourceType {

	/**
	 * Obtains the name of the {@link TeamSource} within the {@link OfficeFloor}
	 * that may be configured.
	 * 
	 * @return Name of the {@link TeamSource} within the {@link OfficeFloor}
	 *         that may be configured.
	 */
	String getOfficeFloorTeamSourceName();

	/**
	 * Obtains the {@link OfficeFloorTeamSourcePropertyType} instances identify
	 * the {@link Property} instances that may be configured for this
	 * {@link TeamSource}.
	 * 
	 * @return {@link OfficeFloorTeamSourcePropertyType} instances.
	 */
	OfficeFloorTeamSourcePropertyType[] getOfficeFloorTeamSourcePropertyTypes();

}