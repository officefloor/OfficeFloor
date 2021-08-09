/*-
 * #%L
 * OpenAPI
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
