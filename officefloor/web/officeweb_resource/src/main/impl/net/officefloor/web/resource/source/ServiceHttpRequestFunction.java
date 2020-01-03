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