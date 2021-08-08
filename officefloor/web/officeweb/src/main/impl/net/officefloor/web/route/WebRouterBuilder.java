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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpMethod.HttpMethodEnum;
import net.officefloor.web.HttpInputPath;
import net.officefloor.web.HttpInputPathImpl;
import net.officefloor.web.HttpInputPathSegment;
import net.officefloor.web.HttpInputPathSegment.HttpInputPathSegmentEnum;

/**
 * Builds the {@link WebRouter}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebRouterBuilder {

	/**
	 * Indicates if the path contains parameters.
	 * 
	 * @param path Path.
	 * @return <code>true</code> should the path contain parameters.
	 */
	public static boolean isPathParameters(String path) {
		return path.contains("{");
	}

	/**
	 * Obtains the context qualified path.
	 * 
	 * @param contextPath Context path. May be <code>null</code>.
	 * @param path        Path.
	 * @return Context qualified path.
	 */
	public static String getContextQualifiedPath(String contextPath, String path) {

		// Ignore / at end of path
		if ((!"/".equals(path)) && (path.endsWith("/"))) {
			path = path.substring(0, path.length() - 1);
		}

		// Include the context path
		if (contextPath != null) {
			path = contextPath + path;
		}

		// Return the context qualified path
		return path;
	}

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
	 * @param contextPath Context path.
	 */
	public WebRouterBuilder(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * Adds a route.
	 *
	 * @param method  {@link HttpMethod}.
	 * @param path    Path. Use <code>{param}</code> to signify path parameters.
	 * @param handler {@link WebRouteHandler} for the route.
	 * @return {@link HttpInputPath} for the route.
	 */
	public HttpInputPath addRoute(HttpMethod method, String path, WebRouteHandler handler) {

		// Keep track of input path
		final String inputPath = path;

		// Obtain the context qualified path
		path = getContextQualifiedPath(this.contextPath, path);

		// Parse out the static segments and parameters from path
		List<HttpInputPathSegment> segments = new ArrayList<>();
		int currentIndex = 0;
		do {

			// Find the next parameter
			int nextParamStart = path.indexOf('{', currentIndex);
			if (nextParamStart < 0) {
				// No further parameters
				String staticContent = path.substring(currentIndex);
				segments.add(new HttpInputPathSegment(HttpInputPathSegmentEnum.STATIC, staticContent));
				currentIndex = path.length();

			} else {
				// Another parameter, so ensure static path separation
				// (also handle parameter at start)
				if ((currentIndex > 0) && ((nextParamStart - currentIndex) == 0)) {
					throw new IllegalArgumentException("Must have static characters between path parameters");
				}

				// Include the static content
				String staticContent = path.substring(currentIndex, nextParamStart);
				segments.add(new HttpInputPathSegment(HttpInputPathSegmentEnum.STATIC, staticContent));

				// Find the end of the parameter
				int nextParamEnd = path.indexOf('}', nextParamStart);
				if (nextParamEnd < 0) {
					throw new IllegalArgumentException("No terminating '}' for parameter");
				}
				String parameterName = path.substring(nextParamStart + 1, nextParamEnd);
				segments.add(new HttpInputPathSegment(HttpInputPathSegmentEnum.PARAMETER, parameterName));

				// Move to after parameter
				currentIndex = nextParamEnd + "}".length();
			}
		} while (currentIndex < path.length());

		// Stitch the segments into linked list
		for (int i = 0; i < (segments.size() - 1); i++) {
			segments.get(i).next = segments.get(i + 1);
		}

		// Create and register the web route
		WebRoute route = new WebRoute(method, inputPath, segments.get(0), handler);
		this.routes.add(route);

		// Return HTTP input path
		return route.createHttpInputPath();
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
			nodes[i] = this.createNode(choices[i], new LinkedList<>(), true);
		}

		// Return the web router
		return new WebRouter(nodes);
	}

	/**
	 * Creates the {@link WebRouteNode}.
	 * 
	 * @param choice              {@link WebRouteChoice}.
	 * @param staticCharacters    Previous static characters.
	 * @param isWildcardOnlyMatch Indicates if wild card only match.
	 * @return {@link WebRouteNode}.
	 */
	private WebRouteNode createNode(WebRouteChoice choice, List<Character> staticCharacters,
			boolean isWildcardOnlyMatch) {

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
				HttpInputPathSegment segment = route.segmentHead;
				while (segment != null) {
					if (segment.type == HttpInputPathSegmentEnum.PARAMETER) {
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

			// Obtain the allowed methods
			Set<String> allowedMethodsSet = new HashSet<>(genericHandling.keySet());
			allowedMethodsSet.add(HttpMethod.OPTIONS.getName());
			if (allowedMethodsSet.contains(HttpMethod.GET.getName())) {
				allowedMethodsSet.add(HttpMethod.HEAD.getName());
			}
			String[] allowedMethods = allowedMethodsSet.stream().sorted().toArray(String[]::new);

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
			return getStaticWrap.apply(new LeafWebRouteNode(allowedMethods, handlers, isWildcardOnlyMatch));

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

				// Determine if wild card only match (taking into account context path)
				isWildcardOnlyMatch = isWildcardOnlyMatch && (this.contextPath != null)
						&& (staticCharacters.size() <= this.contextPath.length());
				return this.createNode(singleChoice, staticCharacters, isWildcardOnlyMatch);

			default:
				// Multiple routes, so create the children
				WebRouteChoice[] childChoices = this.createChoices(choice.routes);
				WebRouteNode[] children = new WebRouteNode[childChoices.length];
				for (int i = 0; i < children.length; i++) {
					children[i] = this.createNode(childChoices[i], new LinkedList<>(), false);
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
					leafNode = (LeafWebRouteNode) this.createNode(paramChoice, new LinkedList<>(), isWildcardOnlyMatch);
					break;

				case STATIC:
					StaticWebRouteNode paramStatic = (StaticWebRouteNode) this.createNode(paramChoice,
							new LinkedList<>(), false);
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
	 * @param routes {@link WebRoute}.
	 * @return {@link WebRouteChoice}.
	 */
	private WebRouteChoice[] createChoices(List<WebRoute> routes) {

		// Create the listing of choices (static always before parameters)
		WebRouteChoice endChoice = null;
		List<WebRouteChoice> staticChoices = new ArrayList<>();
		WebRouteChoice paramChoice = null;

		// Load the route choices
		NEXT_ROUTE: for (WebRoute route : routes) {

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
						continue NEXT_ROUTE;
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
	 * Web route.
	 */
	private static class WebRoute {

		/**
		 * {@link HttpMethod}.
		 */
		private final HttpMethod method;

		/**
		 * Route path.
		 */
		private final String routePath;

		/**
		 * Head {@link HttpInputPathSegment} of linked list of
		 * {@link HttpInputPathSegment} instances.
		 */
		private final HttpInputPathSegment segmentHead;

		/**
		 * Number of static path characters for sorting routes.
		 */
		private final int staticCharacterCount;

		/**
		 * Number of path parameters for sorting routes.
		 */
		private final int parameterCount;

		/**
		 * {@link WebRouteHandler} for the route.
		 */
		private final WebRouteHandler handler;

		/**
		 * Current {@link HttpInputPathSegment}.
		 */
		private HttpInputPathSegment currentSegment;

		/**
		 * Indicates index into the static characters for current
		 * {@link HttpInputPathSegment}.
		 */
		private int staticIndex = -1; // before start

		/**
		 * Instantiate.
		 * 
		 * @param method      {@link HttpMethod}.
		 * @param routePath   Route path.
		 * @param segmentHead Head {@link HttpInputPathSegment} of linked list of
		 *                    {@link HttpInputPathSegment} instances.
		 * @param handler     {@link WebRouteHandler}.
		 */
		public WebRoute(HttpMethod method, String routePath, HttpInputPathSegment segmentHead,
				WebRouteHandler handler) {
			this.method = method;
			this.routePath = routePath;
			this.segmentHead = segmentHead;
			this.handler = handler;

			// Determine the sort metrics
			int staticCharacterCount = 0;
			int parameterCount = 0;
			HttpInputPathSegment segment = this.segmentHead;
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

		/**
		 * Creates the {@link HttpInputPath}.
		 * 
		 * @return {@link HttpInputPath}.
		 */
		public HttpInputPath createHttpInputPath() {
			return new HttpInputPathImpl(this.routePath, this.segmentHead, this.parameterCount);
		}
	}

	/**
	 * Type of {@link WebRouteChoice}.
	 */
	private static enum WebRouteChoiceEnum {
		STATIC, PARAMETER, LEAF
	}

	/**
	 * Choice in route tree.
	 */
	private static class WebRouteChoice {

		/**
		 * {@link WebRouteChoiceEnum}.
		 */
		private final WebRouteChoiceEnum type;

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
		 * @param route {@link WebRoute}.
		 */
		private WebRouteChoice(WebRoute route) {

			// Increment for next character
			route.staticIndex++;

			// Determine if exceed static path length
			if ((route.currentSegment != null) && (route.currentSegment.type == HttpInputPathSegmentEnum.STATIC)) {
				if (route.staticIndex >= route.currentSegment.value.length()) {
					// Move to next segment
					route.currentSegment = route.currentSegment.next;
					route.staticIndex = 0;
				}
			}

			// Configure choice based on current segment
			if (route.currentSegment == null) {
				// End of route
				this.type = WebRouteChoiceEnum.LEAF;
				this.value = null;

			} else {
				// Load static or parameter
				switch (route.currentSegment.type) {
				case STATIC:
					// Add the static route
					this.type = WebRouteChoiceEnum.STATIC;
					this.value = String.valueOf(route.currentSegment.value.charAt(route.staticIndex));
					break;

				case PARAMETER:
					// Add the parameter route
					this.type = WebRouteChoiceEnum.PARAMETER;
					this.value = route.currentSegment.value;

					// Move past parameter segment
					route.currentSegment = route.currentSegment.next;
					route.staticIndex = -1;
					break;

				default:
					throw new Error("Unknown input path segment type " + route.currentSegment.type.name());
				}
			}

			// Include the route
			this.routes.add(route);
		}
	}

}
