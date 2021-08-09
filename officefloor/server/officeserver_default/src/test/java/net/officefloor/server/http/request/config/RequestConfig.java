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

package net.officefloor.server.http.request.config;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration of the HTTP request.
 * 
 * @author Daniel Sagenschneider
 */
public class RequestConfig {

	/**
	 * HTTP method.
	 */
	public String method = "GET";

	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * URI path of the request line.
	 */
	public String path = "";

	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * HTTP version.
	 */
	public String version = "HTTP/1.1";

	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Headers.
	 */
	public final List<HeaderConfig> headers = new LinkedList<HeaderConfig>();

	public void addHeader(HeaderConfig header) {
		this.headers.add(header);
	}

	/**
	 * Body.
	 */
	public String body;

	public void setBody(String body) {
		this.body = body;
	}
}
