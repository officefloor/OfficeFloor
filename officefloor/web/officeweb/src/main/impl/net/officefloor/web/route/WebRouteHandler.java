package net.officefloor.web.route;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.state.HttpArgument;

/**
 * Handles the web route.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebRouteHandler {

	/**
	 * Handles the web route.
	 * 
	 * @param pathArguments
	 *            Head {@link HttpArgument} of the linked list of
	 *            {@link HttpArgument} from the path.
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @param context
	 *            {@link ManagedFunctionContext}.
	 */
	void handle(HttpArgument pathArguments, ServerHttpConnection connection,
			ManagedFunctionContext<?, Indexed> context);

}