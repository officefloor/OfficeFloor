package net.officefloor.server.http.mock;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Mock {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MockServerHttpConnection extends ServerHttpConnection {

	/**
	 * Sends the {@link HttpResponse}.
	 * 
	 * @param escalation
	 *            Optional {@link Escalation}. Should be <code>null</code> for
	 *            successful processing.
	 * @return {@link MockHttpResponse}.
	 */
	MockHttpResponse send(Throwable escalation);

}