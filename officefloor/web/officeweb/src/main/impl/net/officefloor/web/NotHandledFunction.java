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
	public void execute(ManagedFunctionContext<NotHandledDependencies, None> context) throws NotFoundHttpException {

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
	}

}