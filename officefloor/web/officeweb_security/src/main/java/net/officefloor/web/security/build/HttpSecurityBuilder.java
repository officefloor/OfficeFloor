package net.officefloor.web.security.build;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * HTTP security builder.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityBuilder extends PropertyConfigurable {

	/**
	 * Time out in milliseconds to obtain {@link HttpAccessControl} information.
	 * 
	 * @param timeout
	 *            Time out in milliseconds.
	 */
	void setTimeout(long timeout);

	/**
	 * Adds the a <code>Content-Type</code> supported by this
	 * {@link HttpSecurity}.
	 * 
	 * @param contentType
	 *            <code>Content-Type</code> supported by this
	 *            {@link HttpSecurity}.
	 */
	void addContentType(String contentType);

	/**
	 * <p>
	 * Obtains the {@link OfficeSectionInput} to authenticate with application
	 * credentials.
	 * <p>
	 * The application credentials are to be a parameter to this
	 * {@link OfficeSectionInput}.
	 * 
	 * @return {@link OfficeSectionInput} to undertake authentication with the
	 *         application credentials..
	 */
	OfficeSectionInput getAuthenticateInput();

	/**
	 * Obtains the {@link OfficeSectionOutput} from the {@link HttpSecurity}.
	 * 
	 * @param outputName
	 *            {@link OfficeSectionOutput} name.
	 * @return {@link OfficeSectionOutput} for the name.
	 */
	OfficeSectionOutput getOutput(String outputName);

	/**
	 * Creates a {@link HttpSecurer} for this {@link HttpSecurity}.
	 * 
	 * @param securable
	 *            {@link HttpSecurable} to provide the access configuration. May
	 *            be <code>null</code> to just require authentication.
	 * @return {@link HttpSecurer}.
	 */
	HttpSecurer createHttpSecurer(HttpSecurable securable);

}