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
 * <p>
 * Cookie on the {@link HttpRequest}.
 * <p>
 * As per <a href="https://tools.ietf.org/html/rfc6265">RFC-6265</a>.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequestCookie {

	/**
	 * Obtains the name.
	 * 
	 * @return Name.
	 */
	String getName();

	/**
	 * Obtains the value.
	 * 
	 * @return Value.
	 */
	String getValue();

}
