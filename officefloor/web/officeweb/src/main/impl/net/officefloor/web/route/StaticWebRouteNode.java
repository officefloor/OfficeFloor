/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
	 * @param characters
	 *            Static characters.
	 * @param nodes
	 *            Further {@link WebRouteNode} instances.
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
	public boolean handle(HttpMethod method, String path, int index, HttpArgument headPathParameter,
			ServerHttpConnection connection, ManagedFunctionContext<?, Indexed> context) {

		// Determine if enough characters
		if (this.characters.length + index > path.length()) {
			return false; // not enough characters to match
		}

		// Determine if match on characters
		for (int i = 0; i < this.characters.length; i++) {
			char staticCharacter = this.characters[i];
			char pathCharacter = path.charAt(index + i);
			if (staticCharacter != pathCharacter) {
				return false; // not match static route
			}
		}

		// As here, match on paths, so continue matching
		for (int i = 0; i < this.nodes.length; i++) {
			WebRouteNode node = this.nodes[i];
			if (node.handle(method, path, index + this.characters.length, headPathParameter, connection, context)) {
				return true; // handled
			}
		}

		// As here, not handled by this branch of route tree
		return false;
	}

}