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
package net.officefloor.web;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.escalation.NotFoundHttpException;
import net.officefloor.web.route.WebServicer;

/**
 * {@link ManagedFunction} for not handling routing.
 * 
 * @author Daniel Sagenschneider
 */
public class NotHandledFunction implements ManagedFunctionFactory<NotHandledFunction.NotHandledDependencies, None>,
		ManagedFunction<NotHandledFunction.NotHandledDependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum NotHandledDependencies {
		SERVER_HTTP_CONNECTION, WEB_SERVICER
	}

	/*
	 * ================== ManagedFunctionFactory =========================
	 */

	@Override
	public ManagedFunction<NotHandledDependencies, None> createManagedFunction() {
		return this;
	}

	/*
	 * ==================== ManagedFunction ==============================
	 */

	@Override
	public Object execute(ManagedFunctionContext<NotHandledDependencies, None> context) throws NotFoundHttpException {

		// Obtain details
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(NotHandledDependencies.SERVER_HTTP_CONNECTION);
		WebServicer servicer = (WebServicer) context.getObject(NotHandledDependencies.WEB_SERVICER);

		// Service request (if available)
		if (servicer != null) {
			servicer.service(connection);

		} else {
			// Provide default not match servicing
			WebServicer.NO_MATCH.service(connection);
		}

		// Nothing further
		return null;
	}

}