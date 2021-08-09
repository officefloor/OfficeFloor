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

package net.officefloor.web.route;

import java.util.function.Function;

import net.officefloor.server.http.HttpMethod;

/**
 * Handling details for the {@link LeafWebRouteNode}.
 * 
 * @author Daniel Sagenschneider
 */
public class LeafWebRouteHandling {

	/**
	 * Factory for the parameter names.
	 */
	public final Function<HttpMethod, String[]> parameterNamesFactory;

	/**
	 * Factory to create the {@link WebRouteHandler}.
	 */
	public final Function<HttpMethod, WebRouteHandler> handlerFactory;

	/**
	 * Instantiate.
	 * 
	 * @param parameterNamesFactory
	 *            Factory for the parameter names.
	 * @param handlerFactory
	 *            Factory to create the {@link WebRouteHandler}.
	 */
	public LeafWebRouteHandling(Function<HttpMethod, String[]> parameterNamesFactory,
			Function<HttpMethod, WebRouteHandler> handlerFactory) {
		this.parameterNamesFactory = parameterNamesFactory;
		this.handlerFactory = handlerFactory;
	}

}
