package net.officefloor.example.weborchestration;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * <p>
 * Handles HEAD method requests.
 * <p>
 * Necessary for the Selenium tests (which send HEAD and GET requests).
 * 
 * @author daniel
 */
public class HeadMethodHandler {

	/**
	 * Flows for the <code>handleHead</code> method.
	 */
	@FlowInterface
	public static interface HandleHeadFlows {
		void doHead();

		void doOther();
	}

	/**
	 * Handles HEAD methos.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 */
	public void handleHead(ServerHttpConnection connection,
			HandleHeadFlows flows) {

		// Log request
		HttpRequest request = connection.getHttpRequest();
		String method = request.getMethod();
		System.out.println("REQUEST: " + method + " " + request.getRequestURI()
				+ " " + request.getVersion());
		for (HttpHeader header : request.getHeaders()) {
			System.out.println("    " + header.getName() + ": "
					+ header.getValue());
		}

		// Determine if HEAD method
		if ("HEAD".equalsIgnoreCase(method)) {
			// HEAD method
			flows.doHead();
		} else {
			// Other method
			flows.doOther();
		}
	}

}