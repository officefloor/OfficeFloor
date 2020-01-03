package net.officefloor.web.spi.security;

import java.io.Serializable;

/**
 * HTTP ratify context.
 * 
 * @author Daniel Sagenschneider
 */
public interface RatifyContext<AC extends Serializable> extends HttpSecurityActionContext, AccessControlListener<AC> {
}