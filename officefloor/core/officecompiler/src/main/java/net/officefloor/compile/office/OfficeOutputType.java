package net.officefloor.compile.office;

import net.officefloor.compile.spi.office.OfficeOutput;
import net.officefloor.frame.api.manage.Office;

/**
 * <code>Type definition</code> of an {@link OfficeOutput} from the
 * {@link Office}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeOutputType {

	/**
	 * Obtains the name of {@link OfficeOutput}.
	 * 
	 * @return Name of this {@link OfficeOutput}.
	 */
	String getOfficeOutputName();

	/**
	 * Obtains the fully qualified class name of the argument type from this
	 * {@link OfficeOutput}.
	 * 
	 * @return Argument type to this {@link OfficeOutput}.
	 */
	String getArgumentType();

}