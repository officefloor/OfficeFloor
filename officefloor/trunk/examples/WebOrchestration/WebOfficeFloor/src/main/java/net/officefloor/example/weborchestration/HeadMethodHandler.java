/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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