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
package net.officefloor.web;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.web.route.WebRouteHandler;
import net.officefloor.web.state.HttpArgument;

/**
 * {@link WebRouteHandler} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WebRouteHandlerImpl implements WebRouteHandler {

	/**
	 * {@link Flow} index for handling.
	 */
	private final int flowIndex;

	/**
	 * Instantiate.
	 * 
	 * @param flowIndex
	 *            {@link Flow} index for handling.
	 */
	public WebRouteHandlerImpl(int flowIndex) {
		this.flowIndex = flowIndex;
	}

	/*
	 * ================== WebRouteHandler ===================
	 */

	@Override
	public void handle(HttpArgument pathArguments, ManagedFunctionContext<?, Indexed> context) {
		context.doFlow(this.flowIndex, pathArguments, null);
	}

}