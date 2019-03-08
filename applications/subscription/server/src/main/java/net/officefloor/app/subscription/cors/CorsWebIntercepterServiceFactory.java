package net.officefloor.app.subscription.cors;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.WebInterceptServiceFactory;

/**
 * CORS {@link WebInterceptServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class CorsWebIntercepterServiceFactory implements WebInterceptServiceFactory {

	public static class CorsWebIntercepter {
		@NextFunction("service")
		public static void cors(ServerHttpConnection connection) {
			Cors.options(connection);
		}
	}

	@Override
	public Class<?> createService(ServiceContext context) throws Throwable {
		return CorsWebIntercepter.class;
	}

}