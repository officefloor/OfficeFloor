package net.officefloor.web.security.impl;

import java.io.Serializable;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.AccessControlListener;
import net.officefloor.web.state.HttpRequestState;

/**
 * {@link ManagedFunction} logout context.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionLogoutContext<AC extends Serializable> extends AccessControlListener<AC> {

	/**
	 * Obtains the {@link ServerHttpConnection} to be secured.
	 * 
	 * @return {@link ServerHttpConnection}.
	 */
	ServerHttpConnection getConnection();

	/**
	 * Obtains the {@link HttpSession}.
	 * 
	 * @return {@link HttpSession}.
	 */
	HttpSession getSession();

	/**
	 * Obtains the {@link HttpRequestState}.
	 * 
	 * @return {@link HttpRequestState}.
	 */
	HttpRequestState getRequestState();

}