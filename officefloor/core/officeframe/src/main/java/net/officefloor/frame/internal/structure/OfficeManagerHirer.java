package net.officefloor.frame.internal.structure;

/**
 * Hires a new {@link OfficeManager}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeManagerHirer {

	/**
	 * Hires a new {@link OfficeManager}.
	 * 
	 * @param managingProcess {@link ProcessState} to manage the
	 *                        {@link OfficeManager}.
	 * @return New {@link OfficeManager}.
	 */
	OfficeManager hireOfficeManager(ProcessState managingProcess);

}