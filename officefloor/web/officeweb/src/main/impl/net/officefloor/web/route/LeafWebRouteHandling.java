package net.officefloor.web.route;

import java.util.function.Function;

import net.officefloor.server.http.HttpMethod;

/**
 * Handling details for the {@link LeafWebRouteNode}.
 * 
 * @author Daniel Sagenschneider
 */
public class LeafWebRouteHandling {

	/**
	 * Factory for the parameter names.
	 */
	public final Function<HttpMethod, String[]> parameterNamesFactory;

	/**
	 * Factory to create the {@link WebRouteHandler}.
	 */
	public final Function<HttpMethod, WebRouteHandler> handlerFactory;

	/**
	 * Instantiate.
	 * 
	 * @param parameterNamesFactory
	 *            Factory for the parameter names.
	 * @param handlerFactory
	 *            Factory to create the {@link WebRouteHandler}.
	 */
	public LeafWebRouteHandling(Function<HttpMethod, String[]> parameterNamesFactory,
			Function<HttpMethod, WebRouteHandler> handlerFactory) {
		this.parameterNamesFactory = parameterNamesFactory;
		this.handlerFactory = handlerFactory;
	}

}