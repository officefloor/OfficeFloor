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