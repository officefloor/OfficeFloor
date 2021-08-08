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

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.state.HttpArgument;

/**
 * Node in the {@link WebRouter} route tree.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebRouteNode {

	/**
	 * Indicates possible matching of {@link WebRouteNode}.
	 */
	public enum WebRouteResultEnum {
		NO_MATCH, MATCH_PATH_NOT_METHOD, MATCH
	}

	/**
	 * Attempts to handle the path.
	 * 
	 * @param method           {@link HttpMethod}.
	 * @param path             Path.
	 * @param index            Index into the path.
	 * @param headPathArgument Head {@link HttpArgument} from the path.
	 * @param connection       {@link ServerHttpConnection}.
	 * @param context          {@link ManagedFunctionContext}.
	 * @return {@link WebServicer}.
	 */
	WebServicer handle(HttpMethod method, String path, int index, HttpArgument headPathArgument,
			ServerHttpConnection connection, ManagedFunctionContext<?, Indexed> context);

}
