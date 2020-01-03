package net.officefloor.compile.office;

import net.officefloor.compile.spi.office.OfficeInput;
import net.officefloor.frame.api.manage.Office;

/**
 * <code>Type definition</code> of an {@link OfficeInput} into the
 * {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeInputType {

	/**
	 * Obtains the name of {@link OfficeInput}.
	 * 
	 * @return Name of this {@link OfficeInput}.
	 */
	String getOfficeInputName();

	/**
	 * Obtains the fully qualified class name of the parameter type to this
	 * {@link OfficeInput}.
	 * 
	 * @return Parameter type to this {@link OfficeInput}.
	 */
	String getParameterType();

}