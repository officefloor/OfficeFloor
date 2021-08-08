/*-
 * #%L
 * HTTP Server
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

package net.officefloor.server.http;

import java.io.IOException;

import net.officefloor.frame.api.escalate.Escalation;

/**
 * Enables sending an appropriate response for an {@link Escalation}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpEscalationHandler {

	/**
	 * Handles the {@link Escalation}.
	 * 
	 * @param context
	 *            {@link HttpEscalationContext}.
	 * @return <code>true</code> if handled {@link Escalation} into the
	 *         {@link HttpResponse}. <code>false</code> if not able to handle
	 *         the particular {@link Escalation}.
	 * @throws IOException
	 *             If fails to write the {@link Escalation}.
	 */
	boolean handle(HttpEscalationContext context) throws IOException;

}
