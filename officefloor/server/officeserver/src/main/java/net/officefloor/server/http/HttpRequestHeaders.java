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

/**
 * {@link HttpHeader} instances for the {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequestHeaders extends Iterable<HttpHeader> {

	/**
	 * Obtains the first {@link HttpHeader} by the name.
	 * 
	 * @param name
	 *            Name of the {@link HttpHeader}.
	 * @return First {@link HttpHeader} or <code>null</code> if no
	 *         {@link HttpHeader} by the name.
	 */
	HttpHeader getHeader(CharSequence name);

	/**
	 * Obtains all the {@link HttpHeader} instances by the name.
	 * 
	 * @param name
	 *            Name of the {@link HttpHeader} instances.
	 * @return All {@link HttpHeader} instances by the name.
	 */
	Iterable<HttpHeader> getHeaders(CharSequence name);

	/**
	 * Obtains the {@link HttpHeader} at the index.
	 * 
	 * @param index
	 *            Index of the {@link HttpHeader}.
	 * @return {@link HttpHeader} at the index.
	 */
	HttpHeader headerAt(int index);

	/**
	 * Obtains the number of {@link HttpHeader} instances.
	 * 
	 * @return Number of {@link HttpHeader} instances.
	 */
	int length();

}
