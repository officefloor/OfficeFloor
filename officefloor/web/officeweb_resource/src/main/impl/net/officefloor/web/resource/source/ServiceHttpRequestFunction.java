/*-
 * #%L
 * Web resources
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
