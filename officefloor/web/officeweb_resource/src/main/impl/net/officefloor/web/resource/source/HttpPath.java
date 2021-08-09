/*-
 * #%L
 * Web resources
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

package net.officefloor.web.resource.source;

import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.route.WebServicer;

/**
 * Path for a {@link HttpResource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpPath {

	/**
	 * Path.
	 */
	private final String path;

	/**
	 * {@link WebServicer}.
	 */
	private final WebServicer webServicer;

	/**
	 * Instantiate.
	 * 
	 * @param path Path.
	 */
	public HttpPath(String path) {
		this.path = path;
		this.webServicer = null;
	}

	/**
	 * Instantiate.
	 * 
	 * @param request {@link HttpRequest} to extract the path.
	 */
	public HttpPath(HttpRequest request, WebServicer webServicer) {
		this.path = request.getUri();
		this.webServicer = webServicer;
	}

	/**
	 * Obtains the path.
	 * 
	 * @return Path.
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * Obtains the {@link WebServicer}.
	 * 
	 * @return {@link WebServicer}.
	 */
	public WebServicer getWebServicer() {
		return this.webServicer;
	}

}
