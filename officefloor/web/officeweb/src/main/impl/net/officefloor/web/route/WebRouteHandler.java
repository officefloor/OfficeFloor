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

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
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
	 * @param context
	 *            {@link ManagedFunctionContext}.
	 */
	void handle(HttpArgument pathArguments, ManagedFunctionContext<None, Indexed> context);

}