package net.officefloor.web.accept;

import net.officefloor.server.http.HttpRequest;

/**
 * Negotiates the acceptable handler for the {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AcceptNegotiator<H> {

	/**
	 * Obtains the acceptable handler for the {@link HttpRequest}.
	 * 
	 * @param request
	 *            {@link HttpRequest}.
	 * @return Acceptable handler, or <code>null</code> if no acceptable
	 *         handler.
	 */
	H getHandler(HttpRequest request);

}