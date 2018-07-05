/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
	 * Attempts to handle the path.
	 * 
	 * @param method
	 *            {@link HttpMethod}.
	 * @param path
	 *            Path.
	 * @param index
	 *            Index into the path.
	 * @param headPathArgument
	 *            Head {@link HttpArgument} from the path.
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @param context
	 *            {@link ManagedFunctionContext}.
	 * @return <code>true</code> if handled the path.
	 */
	boolean handle(HttpMethod method, String path, int index, HttpArgument headPathArgument,
			ServerHttpConnection connection, ManagedFunctionContext<?, Indexed> context);

}