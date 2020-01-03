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