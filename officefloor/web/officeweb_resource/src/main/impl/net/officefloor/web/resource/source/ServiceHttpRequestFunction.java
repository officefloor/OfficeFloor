/*-
 * #%L
 * Web resources
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

package net.officefloor.web.resource.source;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResourceStore;
import net.officefloor.web.route.WebServicer;

/**
 * {@link ManagedFunction} to send the {@link HttpFile} from the
 * {@link HttpResourceStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServiceHttpRequestFunction extends StaticManagedFunction<ServiceHttpRequestFunction.Dependencies, None> {

	/**
	 * Dependencies.
	 */
	public static enum Dependencies {
		SERVER_HTTP_CONNECTION, WEB_SERVICER
	}

	/*
	 * ==================== ManagedFunction ==================
	 */

	@Override
	public void execute(ManagedFunctionContext<Dependencies, None> context) throws Exception {

		// Obtain the dependencies
		ServerHttpConnection connection = (ServerHttpConnection) context.getObject(Dependencies.SERVER_HTTP_CONNECTION);
		WebServicer webServicer = (WebServicer) context.getObject(Dependencies.WEB_SERVICER);

		// Trigger servicing HTTP request
		context.setNextFunctionArgument(new HttpPath(connection.getRequest(), webServicer));
	}

}
