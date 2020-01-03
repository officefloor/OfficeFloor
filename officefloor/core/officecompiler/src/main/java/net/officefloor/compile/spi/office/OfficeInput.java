package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.manage.Office;

/**
 * Input to the {@link Office}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeInput extends OfficeFlowSourceNode {

	/**
	 * Obtains the name of this {@link OfficeInput}.
	 * 
	 * @return Name of this {@link OfficeInput}.
	 */
	String getOfficeInputName();

}
