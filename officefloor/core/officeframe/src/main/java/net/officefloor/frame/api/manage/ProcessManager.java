package net.officefloor.frame.api.manage;

import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Manages the {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcessManager {

	/**
	 * Cancels execution of the {@link ProcessState}.
	 */
	void cancel();

}