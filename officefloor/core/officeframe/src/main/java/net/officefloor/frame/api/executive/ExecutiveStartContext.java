package net.officefloor.frame.api.executive;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.OfficeManager;

/**
 * Context for {@link Executive} to start managing the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveStartContext {

	/**
	 * Obtains the default {@link OfficeManager} instances.
	 * 
	 * @return Default {@link OfficeManager} instances.
	 */
	OfficeManager[] getDefaultOfficeManagers();

}