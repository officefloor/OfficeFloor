package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;

/**
 * Start-up trigger for a {@link ManagedFunction} within the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeStart extends OfficeFlowSourceNode {

	/**
	 * Obtains the name of this {@link Office} start-up trigger.
	 * 
	 * @return Name of this {@link Office} start-up trigger.
	 */
	String getOfficeStartName();

}