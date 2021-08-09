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
import net.officefloor.server.http.ServerHttpConnection;
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
	 * {@link Map} of terminating {@link Character} to {@link StaticWebRouteNode}
	 * instances.
	 */
	private final Map<Character, WebRouteNode[]> characterToChildren;

	/**
	 * {@link LeafWebRouteNode}.
	 */
	private LeafWebRouteNode leafNode;

	/**
	 * Instantiate.
	 * 
	 * @param nodes    Further {@link StaticWebRouteNode} instances.
	 * @param leafNode {@link LeafWebRouteNode} should the parameter finish the
	 *                 path. May be <code>null</code> if parameter is always
	 *                 embedded in middle of the path.
	 */
	public ParameterWebRouteNode(StaticWebRouteNode[] nodes, LeafWebRouteNode leafNode) {
		this.leafNode = leafNode;

		// Create the terminating characters and map to children
		Set<Character> initialCharacters = new HashSet<>();
		Map<Character, List<WebRouteNode>> characterNodesMap = new HashMap<>();
		for (StaticWebRouteNode node : nodes) {
			char initialCharacter = node.getInitialCharacter();

			// Add mapping of child
			List<WebRouteNode> characterNodes = characterNodesMap.get(initialCharacter);
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
	 * @param headPathArgument Head {@link HttpArgument}.of arguments.
	 * @param argumentValue    {@link HttpArgument} value.
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
	public WebServicer handle(HttpMethod method, String path, int index, HttpArgument headPathArgument,
			ServerHttpConnection connection, ManagedFunctionContext<?, Indexed> context) {

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
					WebServicer servicer = WebServicer.getBestMatch(method, path, index,
							this.includePathArgument(headPathArgument, parameterValue), connection, context, nodes);
					if (WebServicer.isMatch(servicer)) {
						return servicer; // parameter terminated (route matched)
					}
				}
			}

			// Increment for next character
			index++;
		}

		// As here, reached end of path
		if (this.leafNode != null) {

			// Ignore trailing '/' characters (also handle / for {path} match)
			int parameterEnd = index - 1; // last index
			while ((parameterEnd > parameterStart) && (path.charAt(parameterEnd) == '/')) {
				parameterEnd--;
			}

			// Obtain the parameter value (+1 as exclusive)
			String parameterValue = path.substring(parameterStart, parameterEnd + 1);

			// Handle by leaf
			return this.leafNode.handle(method, path, index, this.includePathArgument(headPathArgument, parameterValue),
					connection, context);
		}

		// As here, no match
		return WebServicer.NO_MATCH;
	}

}
