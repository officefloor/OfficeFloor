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

package net.officefloor.web.openapi.operation;

import io.swagger.v3.oas.models.Operation;

/**
 * Extension to configure the {@link Operation}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OpenApiOperationExtension {

	/**
	 * Creates the {@link OpenApiOperationBuilder} for the {@link Operation}.
	 * 
	 * @param context {@link OpenApiOperationContext}.
	 * @return {@link OpenApiOperationBuilder} or <code>null</code> if not handle
	 *         {@link Operation}.
	 * @throws Exception If fails to create the {@link OpenApiOperationBuilder}.
	 */
	OpenApiOperationBuilder createBuilder(OpenApiOperationContext context) throws Exception;

}
