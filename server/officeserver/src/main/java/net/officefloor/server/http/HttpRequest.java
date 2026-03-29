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

import net.officefloor.server.stream.ServerInputStream;

/**
 * HTTP request from the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequest {

	/**
	 * Obtains the {@link HttpMethod}.
	 * 
	 * @return {@link HttpMethod}.
	 */
	HttpMethod getMethod();

	/**
	 * Obtains the request URI as provided on the request.
	 * 
	 * @return Request URI as provided on the request.
	 */
	String getUri();

	/**
	 * Obtains the {@link HttpVersion}.
	 * 
	 * @return {@link HttpVersion}.
	 */
	HttpVersion getVersion();

	/**
	 * Obtains the {@link HttpRequestHeaders}.
	 * 
	 * @return {@link HttpRequestHeaders}.
	 */
	HttpRequestHeaders getHeaders();

	/**
	 * Obtains the {@link HttpRequestCookies}.
	 * 
	 * @return {@link HttpRequestCookies}.
	 */
	HttpRequestCookies getCookies();

	/**
	 * Obtains the {@link ServerInputStream} to the entity of the HTTP request.
	 * 
	 * @return {@link ServerInputStream} to the entity of the HTTP request.
	 */
	ServerInputStream getEntity();

}
