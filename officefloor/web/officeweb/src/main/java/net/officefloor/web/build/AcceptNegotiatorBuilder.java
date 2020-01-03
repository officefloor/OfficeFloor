package net.officefloor.web.build;

import net.officefloor.web.accept.AcceptNegotiator;

/**
 * Builds the {@link AcceptNegotiator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AcceptNegotiatorBuilder<H> {

	/**
	 * Adds a handler.
	 * 
	 * @param contentType
	 *            <code>Content-Type</code> handled by the handler. This may
	 *            include wild cards. For example: <code>image/*</code>
	 * @param handler
	 *            Handler.
	 */
	void addHandler(String contentType, H handler);

	/**
	 * Builds the {@link AcceptNegotiator}.
	 * 
	 * @return {@link AcceptNegotiator}.
	 * @throws NoAcceptHandlersException
	 *             If no handlers configured.
	 */
	AcceptNegotiator<H> build() throws NoAcceptHandlersException;

}