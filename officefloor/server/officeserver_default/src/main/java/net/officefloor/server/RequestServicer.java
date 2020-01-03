package net.officefloor.server;

import net.officefloor.frame.api.manage.ProcessManager;

/**
 * Services requests.
 * 
 * @author Daniel Sagenschneider
 */
public interface RequestServicer<R> {

	/**
	 * Services the request.
	 * 
	 * @param request        Request.
	 * @param responseWriter {@link ResponseWriter}. To enable pipelining of
	 *                       requests, this {@link ResponseWriter} must be invoked
	 *                       to indicate the request has been serviced (even if no
	 *                       data to send).
	 * @return {@link ProcessManager} for servicing the request.
	 */
	ProcessManager service(R request, ResponseWriter responseWriter);

}