/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.build;

import net.officefloor.web.accept.AcceptNegotiator;

/**
 * Builds the {@link AcceptNegotiator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AcceptNegotiatorBuilder<H> {

	/**
	 * Adds a handler.
	 * 
	 * @param contentType
	 *            <code>Content-Type</code> handled by the handler. This may
	 *            include wild cards. For example: <code>image/*</code>
	 * @param handler
	 *            Handler.
	 */
	void addHandler(String contentType, H handler);

	/**
	 * Builds the {@link AcceptNegotiator}.
	 * 
	 * @return {@link AcceptNegotiator}.
	 * @throws NoAcceptHandlersException
	 *             If no handlers configured.
	 */
	AcceptNegotiator<H> build() throws NoAcceptHandlersException;

}
