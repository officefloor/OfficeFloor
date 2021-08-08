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

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * <p>
 * Dependency injected interface to send the {@link Object} response.
 * <p>
 * See {@link HttpResponse} for decorating the HTTP response. May also inject
 * {@link ServerHttpConnection} for dynamic decorating.
 * 
 * @author Daniel Sagenschneider
 */
public interface ObjectResponse<T> {

	/**
	 * Sends the {@link Object}.
	 * 
	 * @param object {@link Object} to send as response.
	 * @throws HttpException If fails to send the {@link Object}.
	 */
	void send(T object) throws HttpException;

}
