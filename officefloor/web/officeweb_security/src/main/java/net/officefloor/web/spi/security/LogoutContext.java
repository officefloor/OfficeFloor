package net.officefloor.web.spi.security;

/**
 * Context for logging out.
 * 
 * @author Daniel Sagenschneider
 */
public interface LogoutContext<O extends Enum<O>, F extends Enum<F>>
		extends HttpSecurityActionContext, HttpSecurityApplicationContext<O, F> {
}