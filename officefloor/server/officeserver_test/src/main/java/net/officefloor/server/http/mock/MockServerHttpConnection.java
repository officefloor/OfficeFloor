/*-
 * #%L
 * Testing of HTTP Server
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
