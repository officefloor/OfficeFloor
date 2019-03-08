package net.officefloor.web;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.build.WebInterceptServiceFactory;

/**
 * Mock {@link WebInterceptServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWebInterceptServiceFactory implements WebInterceptServiceFactory {

	/**
	 * Interceptor {@link Class}.
	 */
	public static Class<?> interceptor = null;

	/*
	 * ================== WebInterceptServiceFactory ======================
	 */

	@Override
	public Class<?> createService(ServiceContext context) throws Throwable {
		return interceptor == null ? Void.TYPE : interceptor;
	}

}