/*-
 * #%L
 * HttpServlet adapter for OfficeFloor HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server.http.servlet;

import javax.servlet.http.HttpServlet;

import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;

/**
 * Bridge from {@link HttpServlet} to {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletOfficeFloorBridge {

	/**
	 * {@link HttpServerLocation}.
	 */
	private final HttpServerLocation location;

	/**
	 * Indicates if includes {@link Escalation} stack trace.
	 */
	private final boolean isIncludeEscalationStackTrace;

	/**
	 * {@link ExternalServiceInput}.
	 */
	@SuppressWarnings("rawtypes")
	private final ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> input;

	/**
	 * Instantiate.
	 * 
	 * @param location                      {@link HttpServerLocation}.
	 * @param isIncludeEscalationStackTrace Indicates if includes {@link Escalation}
	 *                                      stack trace.
	 * @param input                         {@link ExternalServiceInput}.
	 */
	@SuppressWarnings("rawtypes")
	public HttpServletOfficeFloorBridge(HttpServerLocation location, boolean isIncludeEscalationStackTrace,
			ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> input) {
		this.location = location;
		this.isIncludeEscalationStackTrace = isIncludeEscalationStackTrace;
		this.input = input;
	}

	/**
	 * Obtains the {@link HttpServerLocation}.
	 * 
	 * @return {@link HttpServerLocation}.
	 */
	public HttpServerLocation getHttpServerLocation() {
		return this.location;
	}

	/**
	 * Indicates if include {@link Escalation} stack trace.
	 * 
	 * @return Indicates if include {@link Escalation} stack trace.
	 */
	public boolean isIncludeEscalationStackTrace() {
		return this.isIncludeEscalationStackTrace;
	}

	/**
	 * Obtains the {@link ExternalServiceInput}.
	 * 
	 * @return {@link ExternalServiceInput}.
	 */
	@SuppressWarnings("rawtypes")
	public ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> getInput() {
		return this.input;
	}

}
