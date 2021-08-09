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

/**
 * Factory to create the {@link RequestServicer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RequestServicerFactory<R> {

	/**
	 * Creates the {@link RequestServicer} for the {@link SocketServicer}.
	 * 
	 * @param socketServicer
	 *            {@link SocketServicer}.
	 * @return {@link RequestServicer}.
	 */
	RequestServicer<R> createRequestServicer(SocketServicer<R> socketServicer);

}
