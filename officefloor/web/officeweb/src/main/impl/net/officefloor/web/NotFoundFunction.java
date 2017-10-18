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

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.escalation.NotFoundHttpException;

/**
 * {@link ManagedFunction} for not handling routing.
 * 
 * @author Daniel Sagenschneider
 */
public class NotFoundFunction implements ManagedFunctionFactory<NotFoundFunction.NotFoundDependencies, None>,
		ManagedFunction<NotFoundFunction.NotFoundDependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum NotFoundDependencies {
		SERVER_HTTP_CONNECTION
	}

	/*
	 * ================== ManagedFunctionFactory =========================
	 */

	@Override
	public ManagedFunction<NotFoundDependencies, None> createManagedFunction() {
		return this;
	}

	/*
	 * ==================== ManagedFunction ==============================
	 */

	@Override
	public Object execute(ManagedFunctionContext<NotFoundDependencies, None> context) throws NotFoundHttpException {

		// Obtain the request path
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(NotFoundDependencies.SERVER_HTTP_CONNECTION);
		String requestPath = connection.getHttpRequest().getRequestURI();

		// Not found
		throw new NotFoundHttpException(requestPath);
	}

}