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
import net.officefloor.web.escalation.NotFoundHttpException;
import net.officefloor.web.state.HttpArgument;

/**
 * Servicer for web route.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebServicer {

	/**
	 * {@link WebServicer} for {@link WebRouteMatchEnum#NO_MATCH}.
	 */
	WebServicer NO_MATCH = new WebServicer() {

		@Override
		public WebRouteMatchEnum getMatchResult() {
			return WebRouteMatchEnum.NO_MATCH;
		}

		@Override
		public void service(ServerHttpConnection connection) {

			// Not found
			String requestPath = connection.getRequest().getUri();
			throw new NotFoundHttpException(requestPath);
		}
	};

	/**
	 * Result of matching.
	 */
	enum WebRouteMatchEnum {
		/**
		 * No match.
		 */
		NO_MATCH(0),

		/**
		 * Match on path but not on the {@link HttpMethod}.
		 */
		NOT_ALLOWED_METHOD(1),

		/**
		 * Match.
		 */
		MATCH(2);

		/**
		 * Rating of match to determine best match with another {@link WebServicer}.
		 */
		private final int matchRating;

		/**
		 * Instantiate.
		 * 
		 * @param matchRating Rating of match to determine best match with another
		 *                    {@link WebServicer}.
		 */
		WebRouteMatchEnum(int matchRating) {
			this.matchRating = matchRating;
		}
	}

	/**
	 * Obtains the {@link WebRouteMatchEnum}.
	 * 
	 * @return {@link WebRouteMatchEnum}.
	 */
	WebRouteMatchEnum getMatchResult();

	/**
	 * Services the {@link ServerHttpConnection}.
	 * 
	 * @param connection {@link ServerHttpConnection}.
	 */
	void service(ServerHttpConnection connection);

	/**
	 * Convenience method to indicate if a match.
	 * 
	 * @param result {@link WebServicer}.
	 * @return <code>true</code> if a match.
	 */
	static boolean isMatch(WebServicer result) {
		return result.getMatchResult() == WebRouteMatchEnum.MATCH;
	}

	/**
	 * Obtains the best {@link WebServicer} for the {@link WebRouteNode} instances.
	 * 
	 * @param method            {@link HttpMethod}.
	 * @param path              Path.
	 * @param index             Index within the path.
	 * @param headPathParameter Head {@link HttpArgument}.
	 * @param connection        {@link ServerHttpConnection}.
	 * @param context           {@link ManagedFunctionContext}.
	 * @param nodes             {@link WebRouteNode} instances to match on.
	 * @return Best {@link WebServicer}.
	 */
	static WebServicer getBestMatch(HttpMethod method, String path, int index, HttpArgument headPathParameter,
			ServerHttpConnection connection, ManagedFunctionContext<?, Indexed> context, WebRouteNode[] nodes) {

		// Match in order of nodes
		WebServicer closeMatch = NO_MATCH;
		for (int i = 0; i < nodes.length; i++) {
			WebRouteNode node = nodes[i];

			// Attempt match
			WebServicer result = node.handle(method, path, index, headPathParameter, connection, context);
			if (isMatch(result)) {
				return result; // found match
			}

			// Capture closest match
			closeMatch = closeMatch.getMatchResult().matchRating >= result.getMatchResult().matchRating ? closeMatch
					: result;
		}

		// Return the close match
		return closeMatch;
	}

}
