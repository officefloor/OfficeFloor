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
 * {@link WebRouteNode} for static characters.
 * 
 * @author Daniel Sagenschneider
 */
public class StaticWebRouteNode implements WebRouteNode {

	/**
	 * Static characters.
	 */
	private final char[] characters;

	/**
	 * Further {@link WebRouteNode} instances.
	 */
	private final WebRouteNode[] nodes;

	/**
	 * Instantiate.
	 * 
	 * @param characters Static characters.
	 * @param nodes      Further {@link WebRouteNode} instances.
	 */
	public StaticWebRouteNode(char[] characters, WebRouteNode[] nodes) {
		this.characters = characters;
		this.nodes = nodes;
	}

	/**
	 * Obtains the initial {@link Character}.
	 * 
	 * @return Initial {@link Character}.
	 */
	public char getInitialCharacter() {
		return this.characters[0];
	}

	/*
	 * ================== WebRouteNode ==================
	 */

	@Override
	public WebServicer handle(HttpMethod method, String path, int index, HttpArgument headPathParameter,
			ServerHttpConnection connection, ManagedFunctionContext<?, Indexed> context) {

		// Determine if enough characters
		if (this.characters.length + index > path.length()) {
			return WebServicer.NO_MATCH; // not enough characters to match
		}

		// Determine if match on characters
		for (int i = 0; i < this.characters.length; i++) {
			char staticCharacter = this.characters[i];
			char pathCharacter = path.charAt(index + i);
			if (staticCharacter != pathCharacter) {
				return WebServicer.NO_MATCH; // not match static route
			}
		}

		// As here, match on paths, so continue matching
		return WebServicer.getBestMatch(method, path, index + this.characters.length, headPathParameter, connection,
				context, this.nodes);
	}

}
