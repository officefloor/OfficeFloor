package net.officefloor.compile.spi.officefloor;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link ExecutionStrategy} within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorExecutionStrategy {

	/**
	 * Obtains the name of this {@link OfficeFloorExecutionStrategy}.
	 * 
	 * @return Name of this {@link OfficeFloorExecutionStrategy}.
	 */
	String getOfficeFloorExecutionStratgyName();

}