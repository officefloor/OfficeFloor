package net.officefloor.web.spi.security;

import java.io.Serializable;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.security.HttpAccessControl;

/**
 * Factory for the creation of the {@link HttpAccessControl}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpAccessControlFactory<AC extends Serializable> {

	/**
	 * Creates {@link HttpAccessControl} from the custom access control.
	 * 
	 * @param accessControl
	 *            Custom access control.
	 * @return {@link HttpAccessControl} adapting the custom access control.
	 */
	HttpAccessControl createHttpAccessControl(AC accessControl) throws HttpException;

}