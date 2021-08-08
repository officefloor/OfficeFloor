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
 * Provides a store (or cache) of {@link HttpSession} instances.
 * <p>
 * This interfaces allows customising the storage of {@link HttpSession}
 * instances. Different storage strategies can be used to suit the deployment of
 * the application. For example a different strategy may be chosen if the
 * application is hosted on a single server or across a cluster.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpSessionStore {

	/**
	 * Triggers creating new details for a {@link HttpSession}.
	 *
	 * @param operation
	 *            Operation to receive results of creation.
	 */
	void createHttpSession(CreateHttpSessionOperation operation);

	/**
	 * Triggers retrieving the {@link HttpSession} details.
	 *
	 * @param operation
	 *            Operation to receive results of the retrieval.
	 */
	void retrieveHttpSession(RetrieveHttpSessionOperation operation);

	/**
	 * Triggers storing the {@link HttpSession} details.
	 *
	 * @param operation
	 *            Operations to receive the results of the storage.
	 */
	void storeHttpSession(StoreHttpSessionOperation operation);

	/**
	 * Triggers invalidating the {@link HttpSession}.
	 *
	 * @param operation
	 *            Operation to invalidate the {@link HttpSession}.
	 */
	void invalidateHttpSession(InvalidateHttpSessionOperation operation);

}
