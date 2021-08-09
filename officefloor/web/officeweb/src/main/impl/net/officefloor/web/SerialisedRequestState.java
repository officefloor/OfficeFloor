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

package net.officefloor.web;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.state.HttpRequestState;

/**
 * {@link Serializable} {@link HttpRequest} state.
 * 
 * @author Daniel Sagenschneider
 */
public class SerialisedRequestState implements Serializable {

	/**
	 * Generates identifier for {@link SerialisedRequestState}.
	 */
	private static final AtomicInteger identity = new AtomicInteger(0);

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Identifier for this {@link SerialisedRequestState}.
	 */
	public final int identifier;

	/**
	 * {@link HttpRequestState} momento.
	 */
	public final Serializable momento;

	/**
	 * Instantiate.
	 * 
	 * @param momento
	 *            Momento.
	 */
	public SerialisedRequestState(Serializable momento) {
		this.momento = momento;

		// Generate identifier
		this.identifier = identity.incrementAndGet();
	}

}
