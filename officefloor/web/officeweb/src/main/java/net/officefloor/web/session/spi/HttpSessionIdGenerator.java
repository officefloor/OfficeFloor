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

package net.officefloor.web.session.spi;

import net.officefloor.web.session.HttpSession;

/**
 * <p>
 * Generates the {@link HttpSession} Id.
 * <p>
 * Typically a default {@link HttpSessionIdGenerator} is provided by the
 * {@link HttpSession} and this need not be provided. This interface however
 * enables customising the generation of the {@link HttpSession} Id.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpSessionIdGenerator {

	/**
	 * <p>
	 * Generates the session Id.
	 * <p>
	 * This method may return without the session Id being specified on the
	 * {@link FreshHttpSession}. In this case it is expected that the session Id
	 * will be populated some time in the near future.
	 *
	 * @param session
	 *            {@link FreshHttpSession} to be populated with a new session
	 *            Id.
	 */
	void generateSessionId(FreshHttpSession session);

}
