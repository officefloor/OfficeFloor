package net.officefloor.web.spi.security;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.state.HttpRequestState;

/**
 * Generic context for {@link HttpSecurity} actions.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityActionContext {

	/**
	 * Obtains the {@link ServerHttpConnection}.
	 * 
	 * @return {@link ServerHttpConnection}.
	 */
	ServerHttpConnection getConnection();

	/**
	 * <p>
	 * Qualifies the attribute name to this {@link HttpSecurity} instance.
	 * <p>
	 * Multiple {@link HttpSecuritySource} instances may be registered for the
	 * application. Potentially, some even of the same implementation - likely just
	 * configured differently for different needs.
	 * <p>
	 * Therefore, may use this method to provide a namespace on the attribute to
	 * keep its value isolated to just this instance use of the
	 * {@link HttpSecurity}.
	 * 
	 * @param attributeName Name of the attribute.
	 * @return Qualified attribute name to the {@link HttpSecurity} instance.
	 */
	String getQualifiedAttributeName(String attributeName);

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