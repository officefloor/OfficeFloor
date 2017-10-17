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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpMethod.HttpMethodEnum;

/**
 * Builds the {@link WebRouter}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebRouterBuilder {

	/**
	 * Context path.
	 */
	private final String contextPath;

	/**
	 * {@link WebRoute} instances.
	 */
	private final List<WebRoute> routes = new ArrayList<>();

	/**
	 * Instantiate.
	 * 
	 * @param contextPath
	 *            Context path.
	 */
	public WebRouterBuilder(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * Adds a route.
	 *
	 * @param method
	 *            {@link HttpMethod}.
	 * @param path
	 *            Path. Use <code>{param}</code> to signify path parameters.
	 * @param handler
	 *            {@link WebRouteHandler} for the route.
	 * @return <code>this</code> for builder pattern.
	 */
	public WebRouterBuilder addRoute(HttpMethod method, String path, WebRouteHandler handler) {

		// Ignore / at end of path
		if ((!"/".equals(path)) && (path.endsWith("/"))) {
			path = path.substring(0, path.length() - 1);
		}

		// Include the context path
		if (this.contextPath != null) {
			path = this.contextPath + path;
		}

		// Parse out the static segments and parameters from path
		List<PathSegment> segments = new ArrayList<>();
		int currentIndex = 0;
		do {

			// Find the next parameter
			int nextParamStart = path.indexOf('{', currentIndex);
			if (nextParamStart < 0) {
				// No further parameters
				String staticContent = path.substring(currentIndex);
				segments.add(new PathSegment(PathTypeEnum.STATIC, staticContent));
				currentIndex = path.length();

			} else {
				// Another parameter, so ensure static path separation
				if ((nextParamStart - currentIndex) == 0) {
					throw new IllegalArgumentException("Must have static characters between path parameters");
				}

				// Include the static content
				String staticContent = path.substring(currentIndex, nextParamStart);
				segments.add(new PathSegment(PathTypeEnum.STATIC, staticContent));

				// Find the end of the parameter
				int nextParamEnd = path.indexOf('}', nextParamStart);
				if (nextParamEnd < 0) {
					throw new IllegalArgumentException("No terminating '}' for parameter");
				}
				String parameterName = path.substring(nextParamStart + 1, nextParamEnd);
				segments.add(new PathSegment(PathTypeEnum.PARAMETER, parameterName));

				// Move to after parameter
				currentIndex = nextParamEnd + "}".length();
			}
		} while (currentIndex < path.length());

		// Stitch the segments into linked list
		for (int i = 0; i < (segments.size() - 1); i++) {
			segments.get(i).next = segments.get(i + 1);
		}

		// Create and register the web route
		WebRoute route = new WebRoute(method, segments.get(0), handler);
		this.routes.add(route);

		// Return for builder pattern
		return this;
	}

	/**
	 * Builds the {@link WebRouter}.
	 * 
	 * @return {@link WebRouter}.
	 */
	public WebRouter build() {

		// Sort the routes
		this.routes.sort((a, b) -> {

			// Sort first by more static
			int staticCompare = b.staticCharacterCount - a.staticCharacterCount;
			if (staticCompare != 0) {
				// Sorted by static
				return staticCompare;
			}

			// Sort next by less parameters
			int parameterCompare = a.parameterCount - b.parameterCount;
			if (parameterCompare != 0) {
				return parameterCompare;
			}

			// As here, same route weighting
			return 0;
		});

		// Create the route tree of choices
		WebRouteChoice[] choices = this.createChoices(this.routes);

		// Create the route tree
		WebRouteNode[] nodes = new WebRouteNode[choices.length];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = this.createNode(choices[i], new LinkedList<>());
		}

		// Return the web router
		return new WebRouter(nodes);
	}

	/**
	 * Creates the {@link WebRouteNode}.
	 * 
	 * @param choice
	 *            {@link WebRouteChoice}.
	 * @param staticCharacters
	 *            Previous static characters.
	 * @return {@link WebRouteNode}.
	 */
	private WebRouteNode createNode(WebRouteChoice choice, List<Character> staticCharacters) {

		// Supply the characters
		Supplier<char[]> getStatic = () -> {
			char[] characters = new char[staticCharacters.size()];
			for (int i = 0; i < characters.length; i++) {
				characters[i] = staticCharacters.get(i);
			}
			return characters;
		};

		// Wrap node with potential static
		Function<WebRouteNode, WebRouteNode> getStaticWrap = (node) -> {
			char[] characters = getStatic.get();
			if (characters.length == 0) {
				return node; // no need to wrap
			} else {
				// Wrap with static
				return new StaticWebRouteNode(characters, new WebRouteNode[] { node });
			}
		};

		// Branch choice in routing tree
		switch (choice.type) {
		case LEAF:
			// Create the mapping of route to parameter names
			Map<WebRoute, String[]> routeParameterNames = new HashMap<>();
			for (WebRoute route : choice.routes) {
				List<String> parameterNames = new LinkedList<>();
				PathSegment segment = route.segmentHead;
				while (segment != null) {
					if (segment.type == PathTypeEnum.PARAMETER) {
						parameterNames.add(segment.value);
					}
					segment = segment.next;
				}
				routeParameterNames.put(route, parameterNames.toArray(new String[parameterNames.size()]));
			}

			// Create the method handling (by name)
			Map<String, WebRouteHandler> genericHandling = new HashMap<>();
			Map<String, String[]> genericParameterNames = new HashMap<>();
			for (WebRoute route : choice.routes) {
				String methodName = route.method.getName();
				genericHandling.put(methodName, route.handler);
				String[] parameterNames = routeParameterNames.get(route);
				genericParameterNames.put(methodName, parameterNames);
			}
			Map<HttpMethodEnum, LeafWebRouteHandling> handlers = new EnumMap<>(HttpMethodEnum.class);
			handlers.put(HttpMethodEnum.OTHER,
					new LeafWebRouteHandling((method) -> genericParameterNames.get(method.getName()),
							(method) -> genericHandling.get(method.getName())));

			// Load handling by enum
			for (WebRoute route : choice.routes) {
				String[] parameterNames = routeParameterNames.get(route);
				HttpMethodEnum routeMethod = route.method.getEnum();
				if (routeMethod != HttpMethodEnum.OTHER) {
					handlers.put(routeMethod,
							new LeafWebRouteHandling((method) -> parameterNames, (method) -> route.handler));
				}
			}

			// Return the leaf node
			return getStaticWrap.apply(new LeafWebRouteNode(handlers));

		case STATIC:
			// Add the character to static routes and continue static route
			char character = choice.value.charAt(0);
			staticCharacters.add(character);

			// Handle based on further routes
			switch (choice.routes.size()) {
			case 0:
				throw new IllegalStateException("Ending static route should always have leaf choice");

			case 1:
				// Single choice, so carry on with static characters
				WebRouteChoice singleChoice = new WebRouteChoice(choice.routes.get(0));
				return this.createNode(singleChoice, staticCharacters);

			default:
				// Multiple routes, so create the children
				WebRouteChoice[] childChoices = this.createChoices(choice.routes);
				WebRouteNode[] children = new WebRouteNode[childChoices.length];
				for (int i = 0; i < children.length; i++) {
					children[i] = this.createNode(childChoices[i], new LinkedList<>());
				}
				char[] characters = getStatic.get();
				return new StaticWebRouteNode(characters, children);
			}

		case PARAMETER:
			// Load children nodes
			LeafWebRouteNode leafNode = null;
			List<StaticWebRouteNode> staticNodes = new LinkedList<>();
			WebRouteChoice[] paramEndChoices = this.createChoices(choice.routes);
			for (WebRouteChoice paramChoice : paramEndChoices) {
				switch (paramChoice.type) {
				case LEAF:
					// Ensure only leaf node
					if (leafNode != null) {
						throw new IllegalStateException("May only have one leaf node after a parameter");
					}
					leafNode = (LeafWebRouteNode) this.createNode(paramChoice, new LinkedList<>());
					break;

				case STATIC:
					StaticWebRouteNode paramStatic = (StaticWebRouteNode) this.createNode(paramChoice,
							new LinkedList<>());
					staticNodes.add(paramStatic);
					break;

				case PARAMETER:
					// Parameters not follow (need static demarcation)
					throw new IllegalStateException(
							"May not have a path parameter directly after another path parameter");
				}
			}

			// Return the parameter node
			return getStaticWrap.apply(new ParameterWebRouteNode(
					staticNodes.toArray(new StaticWebRouteNode[staticNodes.size()]), leafNode));

		default:
			throw new IllegalStateException("Unhandled type " + choice.type);
		}
	}

	/**
	 * Create the {@link WebRouteChoice} values for the {@link WebRoute}.
	 * 
	 * @param routes
	 *            {@link WebRoute}.
	 * @return {@link WebRouteChoice}.
	 */
	private WebRouteChoice[] createChoices(List<WebRoute> routes) {

		// Create the listing of choices (static always before parameters)
		WebRouteChoice endChoice = null;
		List<WebRouteChoice> staticChoices = new ArrayList<>();
		WebRouteChoice paramChoice = null;

		// Load the route choices
		for (WebRoute route : routes) {

			// Create the choice
			WebRouteChoice choice = new WebRouteChoice(route);
			switch (choice.type) {
			case LEAF:
				if (endChoice == null) {
					endChoice = choice;
				} else {
					endChoice.routes.addAll(choice.routes);
				}
				break;

			case PARAMETER:
				if (paramChoice == null) {
					paramChoice = choice;
				} else {
					paramChoice.routes.addAll(choice.routes);
				}
				break;

			case STATIC:
				// Determine if match static character
				for (WebRouteChoice staticChoice : staticChoices) {
					if (staticChoice.value.charAt(0) == choice.value.charAt(0)) {
						staticChoice.routes.add(choice.routes.get(0));
						break;
					}
				}

				// As here, new static route
				staticChoices.add(choice);
				break;
			}
		}

		// Load the choices
		WebRouteChoice[] choices = new WebRouteChoice[(endChoice == null ? 0 : 1) + staticChoices.size()
				+ (paramChoice == null ? 0 : 1)];
		int index = 0;
		if (endChoice != null) {
			choices[index++] = endChoice;
		}
		for (WebRouteChoice staticChoice : staticChoices) {
			choices[index++] = staticChoice;
		}
		if (paramChoice != null) {
			choices[index++] = paramChoice;
		}

		// Return the choices
		return choices;
	}

	/**
	 * Route for {@link WebRouter}.
	 */
	private static class WebRoute {

		/**
		 * {@link HttpMethod}.
		 */
		private final HttpMethod method;

		/**
		 * Head {@link PathSegment} of linked list of {@link PathSegment}
		 * instances.
		 */
		private final PathSegment segmentHead;

		/**
		 * Number of static path characters for sorting routes.
		 */
		private final int staticCharacterCount;

		/**
		 * Number of path parameters for sourting routes.
		 */
		private final int parameterCount;

		/**
		 * {@link WebRouteHandler} for the route.
		 */
		private final WebRouteHandler handler;

		/**
		 * Current {@link PathSegment}.
		 */
		private PathSegment currentSegment;

		/**
		 * Indicates index into the static characters for current
		 * {@link PathSegment}.
		 */
		private int staticIndex = -1; // before start

		/**
		 * Instantiate.
		 * 
		 * @param method
		 *            {@link HttpMethod}.
		 * @param segmentHead
		 *            Head {@link PathSegment} of linked list of
		 *            {@link PathSegment} instances.
		 * @param handler
		 *            {@link WebRouteHandler}.
		 */
		public WebRoute(HttpMethod method, PathSegment segmentHead, WebRouteHandler handler) {
			this.method = method;
			this.segmentHead = segmentHead;
			this.handler = handler;

			// Determine the sort metrics
			int staticCharacterCount = 0;
			int parameterCount = 0;
			PathSegment segment = this.segmentHead;
			while (segment != null) {
				switch (segment.type) {
				case STATIC:
					staticCharacterCount += segment.value.length();
					break;
				case PARAMETER:
					parameterCount++;
					break;
				default:
					throw new IllegalStateException(
							WebRoute.class.getSimpleName() + " should not have segment of type " + segment.type);
				}
				segment = segment.next;
			}
			this.staticCharacterCount = staticCharacterCount;
			this.parameterCount = parameterCount;

			// Load the current segment
			this.currentSegment = this.segmentHead;
		}
	}

	/**
	 * Choice in route tree.
	 */
	private static class WebRouteChoice {

		/**
		 * {@link PathTypeEnum}.
		 */
		private final PathTypeEnum type;

		/**
		 * Static path or parameter name.
		 */
		private final String value;

		/**
		 * {@link WebRoute} instances for this choice.
		 */
		private List<WebRoute> routes = new LinkedList<>();

		/**
		 * Instantiate.
		 * 
		 * @param route
		 *            {@link WebRoute}.
		 */
		private WebRouteChoice(WebRoute route) {

			// Increment for next character
			route.staticIndex++;

			// Determine if exceed static path length
			if ((route.currentSegment != null) && (route.currentSegment.type == PathTypeEnum.STATIC)) {
				if (route.staticIndex >= route.currentSegment.value.length()) {
					// Move to next segment
					route.currentSegment = route.currentSegment.next;
					route.staticIndex = 0;
				}
			}

			// Configure choice based on current segment
			if (route.currentSegment == null) {
				// End of route
				this.type = PathTypeEnum.LEAF;
				this.value = null;

			} else {
				// Load static or parameter
				this.type = route.currentSegment.type;
				if (this.type == PathTypeEnum.STATIC) {
					// Add the static route
					this.value = String.valueOf(route.currentSegment.value.charAt(route.staticIndex));
				} else {
					// Add the parameter route
					this.value = route.currentSegment.value;

					// Move past parameter segment
					route.currentSegment = route.currentSegment.next;
					route.staticIndex = -1;
				}
			}

			// Include the route
			this.routes.add(route);
		}
	}

	/**
	 * Types of {@link PathSegment}.
	 */
	private static enum PathTypeEnum {
		STATIC, PARAMETER, LEAF
	}

	/**
	 * Path segment.
	 */
	private static class PathSegment {

		/**
		 * {@link PathTypeEnum}.
		 */
		private final PathTypeEnum type;

		/**
		 * Static path or parameter name.
		 */
		private final String value;

		/**
		 * Next {@link PathSegment}.
		 */
		private PathSegment next = null;

		/**
		 * Instantiate.
		 * 
		 * @param type
		 *            {@link PathTypeEnum}.
		 * @param value
		 *            Static path or parameter name.
		 */
		public PathSegment(PathTypeEnum type, String value) {
			this.type = type;
			this.value = value;
		}
	}

}