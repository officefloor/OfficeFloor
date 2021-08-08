/*-
 * #%L
 * Default OfficeFloor HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.server;

import java.io.IOException;

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
	 * @throws IOException If fails to service the request. This indicates failure
	 *                     in servicing the connection and hence will close the
	 *                     connection.
	 */
	ProcessManager service(R request, ResponseWriter responseWriter) throws IOException;

}
