/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
