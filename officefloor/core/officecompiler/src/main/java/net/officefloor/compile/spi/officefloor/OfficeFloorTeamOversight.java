package net.officefloor.compile.spi.officefloor;

import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link TeamOversight} within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorTeamOversight {

	/**
	 * Obtains the name of this {@link OfficeFloorTeamOversight}.
	 * 
	 * @return Name of this {@link OfficeFloorTeamOversight}.
	 */
	String getOfficeFloorTeamOversightName();

}