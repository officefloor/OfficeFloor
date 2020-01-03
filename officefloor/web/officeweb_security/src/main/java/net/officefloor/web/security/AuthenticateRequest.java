package net.officefloor.web.security;

/**
 * Request for logging out.
 * 
 * @author Daniel Sagenschneider
 */
public interface AuthenticateRequest {

	/**
	 * Notifies the requester that the authenticate attempt has completed.
	 * 
	 * @param failure
	 *            On failure to authenticate it will be the cause of the
	 *            failure. Note that a null {@link Throwable} does not
	 *            necessarily mean authentication was successful (just the
	 *            attempt has complete).
	 */
	void authenticateComplete(Throwable failure);

}