package net.officefloor.frame.api.executive;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.OfficeManager;

/**
 * {@link Office} context for the {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveOfficeContext {

	/**
	 * Obtains the name of the {@link Office}.
	 * 
	 * @return Name of the {@link Office}.
	 */
	String getOfficeName();

	/**
	 * Hires a new {@link OfficeManager}.
	 * 
	 * @return New {@link OfficeManager}.
	 */
	OfficeManager hireOfficeManager();

}