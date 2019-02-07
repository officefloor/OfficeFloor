package net.officefloor.web.resource.source;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.web.route.WebServicer;

/**
 * Translates the {@link HttpPath} to {@link WebServicer}.
 * 
 * @author Daniel Sagenschneider
 */
public class TranslateHttpPathToWebServicerFunction
		extends StaticManagedFunction<TranslateHttpPathToWebServicerFunction.Dependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		HTTP_PATH
	}

	/*
	 * ==================== ManagedFunction ==================
	 */

	@Override
	public Object execute(ManagedFunctionContext<Dependencies, None> context) throws Throwable {
		HttpPath path = (HttpPath) context.getObject(Dependencies.HTTP_PATH);
		return path.getWebServicer();
	}

}