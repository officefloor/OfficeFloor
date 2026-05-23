package net.officefloor.tutorial.corshttpserver;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.build.WebInterceptServiceFactory;

/**
 * CORS {@link WebInterceptServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class CorsWebIntercepterServiceFactory implements WebInterceptServiceFactory {

	@Override
	public Class<?> createService(ServiceContext context) throws Throwable {
		return Cors.class;
	}

}
// END SNIPPET: tutorial