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

package net.officefloor.web.build;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.value.load.ValueLoader;

/**
 * Parses arguments from the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpArgumentParser extends HttpContentParser {

	/**
	 * Parses the HTTP entity.
	 * 
	 * @param request     {@link HttpRequest}.
	 * @param valueLoader {@link ValueLoader}.
	 * @throws HttpException If fails to parse the {@link ServerHttpConnection}.
	 */
	void parse(HttpRequest request, ValueLoader valueLoader) throws HttpException;

}
