package net.officefloor.tutorial.corshttpserver;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.WebInterceptServiceFactory;

/**
 * CORS {@link WebInterceptServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class CorsWebIntercepterServiceFactory implements WebInterceptServiceFactory {

	public static class CorsWebIntercepter {
		@NextFunction("service")
		public static void cors(ServerHttpConnection connection) {
			Cors.cors(connection);
		}
	}

	@Override
	public Class<?> createService(ServiceContext context) throws Throwable {
		return CorsWebIntercepter.class;
	}

}
// END SNIPPET: tutorial