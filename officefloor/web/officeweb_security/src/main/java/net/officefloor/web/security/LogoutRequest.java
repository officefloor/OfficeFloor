package net.officefloor.web.security;

/**
 * Request for logging out.
 * 
 * @author Daniel Sagenschneider
 */
public interface LogoutRequest {

	/**
	 * Notifies the requester that the log out has completed.
	 * 
	 * @param failure
	 *            On a successful logout this will be <code>null</code>. On
	 *            failure to logout it will be the cause of the failure.
	 */
	void logoutComplete(Throwable failure);

}