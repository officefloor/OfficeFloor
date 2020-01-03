package net.officefloor.web.security.build.office;

import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Provides {@link OfficeAdministration} for the {@link HttpSecurity}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpOfficeSecurer {

	/**
	 * Undertakes securing.
	 * 
	 * @param context
	 *            {@link HttpOfficeSecurerContext}.
	 */
	void secure(HttpOfficeSecurerContext context);

}