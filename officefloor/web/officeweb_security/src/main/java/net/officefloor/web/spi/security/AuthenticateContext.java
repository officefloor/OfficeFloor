package net.officefloor.web.spi.security;

import java.io.Serializable;

/**
 * Context for authentication.
 * 
 * @author Daniel Sagenschneider
 */
public interface AuthenticateContext<AC extends Serializable, O extends Enum<O>, F extends Enum<F>>
		extends HttpSecurityActionContext, HttpSecurityApplicationContext<O, F>, AccessControlListener<AC> {
}