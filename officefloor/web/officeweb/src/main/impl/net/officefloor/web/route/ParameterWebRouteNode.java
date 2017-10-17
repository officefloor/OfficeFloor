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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.state.HttpArgument;

/**
 * {@link WebRouteNode} for a path parameter.
 * 
 * @author Daniel Sagenschneider
 */
public class ParameterWebRouteNode implements WebRouteNode {

	/**
	 * Terminating characters for the parameter.
	 */
	private final char[] terminatingCharacters;

	/**
	 * {@link Map} of terminating {@link Character} to
	 * {@link StaticWebRouteNode} instances.
	 */
	private final Map<Character, WebRouteNode[]> characterToChildren;

	/**
	 * {@link LeafWebRouteNode}.
	 */
	private LeafWebRouteNode leafNode;

	/**
	 * Instantiate.
	 * 
	 * @param nodes
	 *            Further {@link StaticWebRouteNode} instances.
	 * @param leafNode
	 *            {@link LeafWebRouteNode} should the parameter finish the path.
	 *            May be <code>null</code> if parameter is always embedded in
	 *            middle of the path.
	 */
	public ParameterWebRouteNode(StaticWebRouteNode[] nodes, LeafWebRouteNode leafNode) {
		this.leafNode = leafNode;

		// Create the terminating characters and map to children
		Set<Character> initialCharacters = new HashSet<>();
		Map<Character, List<WebRouteNode>> characterNodesMap = new HashMap<>();
		for (StaticWebRouteNode node : nodes) {
			char initialCharacter = node.getInitialCharacter();

			// Add mapping of child
			List<WebRouteNode> characterNodes = characterNodesMap.get(initialCharacters);
			if (characterNodes == null) {
				characterNodes = new LinkedList<>();
				characterNodesMap.put(initialCharacter, characterNodes);
			}
			characterNodes.add(node);

			// Keep track of all initial characters
			initialCharacters.add(initialCharacter);
		}

		// Determine if able to complete path with parameter
		if (this.leafNode != null) {
			for (char pathEndCharacter : new char[] { '?', '#' }) {

				// Add mapping for path completion
				characterNodesMap.put(pathEndCharacter, Arrays.asList(this.leafNode));

				// Include the path end character
				initialCharacters.add(pathEndCharacter);
			}
		}

		// Create the array of terminating characters (faster matching)
		this.terminatingCharacters = new char[initialCharacters.size()];
		int index = 0;
		for (Character character : initialCharacters) {
			this.terminatingCharacters[index++] = character;
		}

		// Create the map of children
		// (size for ASCII characters to index avoiding collisions)
		this.characterToChildren = new HashMap<>(128);
		for (Character initialCharacter : characterNodesMap.keySet()) {
			List<WebRouteNode> children = characterNodesMap.get(initialCharacter);

			// Load the initial character choice
			WebRouteNode[] childrenArray = children.toArray(new WebRouteNode[children.size()]);
			this.characterToChildren.put(initialCharacter, childrenArray);
		}
	}

	/**
	 * Includes the {@link HttpArgument}.
	 * 
	 * @param headPathArgument
	 *            Head {@link HttpArgument}.of arguments.
	 * @param argumentValue
	 *            {@link HttpArgument} value.
	 * @return Head {@link HttpArgument} of arguments including the argument.
	 */
	private HttpArgument includePathArgument(HttpArgument headPathArgument, String argumentValue) {
		HttpArgument argumentIncluded = new HttpArgument(null, argumentValue, HttpValueLocation.PATH);
		argumentIncluded.next = headPathArgument;
		return argumentIncluded;
	}

	/*
	 * ======================== WebRouteNode ==================
	 */

	@Override
	public boolean handle(HttpMethod method, String path, int index, HttpArgument headPathArgument,
			ManagedFunctionContext<?, Indexed> context) {

		// Capture starting position of parameter value
		final int parameterStart = index;

		// Loop until match, or end of path
		while (index < path.length()) {

			// Obtain the current character
			char character = path.charAt(index);

			// Determine if terminating character
			for (int c = 0; c < this.terminatingCharacters.length; c++) {
				char terminatingCharacter = this.terminatingCharacters[c];
				if (character == terminatingCharacter) {

					// Obtain parameter value
					String parameterValue = path.substring(parameterStart, index);

					// Attempt to terminate parameter
					WebRouteNode[] nodes = this.characterToChildren.get(character);
					for (int n = 0; n < nodes.length; n++) {
						WebRouteNode node = nodes[n];

						// Determine if handle route
						if (node.handle(method, path, index, this.includePathArgument(headPathArgument, parameterValue),
								context)) {
							return true; // parameter terminated (route handled)
						}
					}
				}
			}

			// Increment for next character
			index++;
		}

		// As here, reached end of path
		if (this.leafNode != null) {

			// Ignore trailing '/' characters
			int parameterEnd = index - 1; // last index
			while (path.charAt(parameterEnd) == '/') {
				parameterEnd--;
			}

			// Obtain the parameter value (+1 as exclusive)
			String parameterValue = path.substring(parameterStart, parameterEnd + 1);

			// Handle by leaf
			return this.leafNode.handle(method, path, index, this.includePathArgument(headPathArgument, parameterValue),
					context);
		}

		// As here, no match
		return false;
	}

}