package net.officefloor.web.openapi;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.openapi.operation.OpenApiOperationBuilder;
import net.officefloor.web.openapi.operation.OpenApiOperationContext;
import net.officefloor.web.openapi.operation.OpenApiOperationExtension;
import net.officefloor.web.openapi.operation.OpenApiOperationExtensionServiceFactory;

/**
 * Mock {@link OpenApiOperationExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockOpenApiOperationExtension
		implements OpenApiOperationExtension, OpenApiOperationExtensionServiceFactory {

	/**
	 * {@link OpenApiOperationBuilder}.
	 */
	public static OpenApiOperationBuilder operationBuilder = null;

	/*
	 * ================= OpenApiOperationExtensionServiceFactory =================
	 */

	@Override
	public OpenApiOperationExtension createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ==================== OpenApiOperationExtensionService =====================
	 */

	@Override
	public OpenApiOperationBuilder createBuilder(OpenApiOperationContext context) throws Exception {
		return operationBuilder;
	}

}