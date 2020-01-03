package net.officefloor.web.security.impl;

import java.io.Serializable;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.AccessControlListener;
import net.officefloor.web.state.HttpRequestState;

/**
 * {@link ManagedFunction} authentication context.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionAuthenticateContext<AC extends Serializable, C> extends AccessControlListener<AC> {

	/**
	 * Obtains the credentials.
	 * 
	 * @return Credentials. May be <code>null</code> if no credentials are
	 *         available.
	 */
	C getCredentials();

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