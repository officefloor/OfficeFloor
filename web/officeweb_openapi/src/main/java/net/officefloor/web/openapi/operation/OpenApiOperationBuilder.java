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
import net.officefloor.compile.spi.office.ExecutionManagedFunction;

/**
 * Builds the {@link Operation}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OpenApiOperationBuilder {

	/**
	 * <p>
	 * Loads the {@link ExecutionManagedFunction} information.
	 * <p>
	 * This will be invoked for each {@link ExecutionManagedFunction} in the
	 * execution tree.
	 * 
	 * @param context {@link OpenApiOperationFunctionContext}.
	 * @throws Exception If fails to load {@link ExecutionManagedFunction}.
	 */
	void buildInManagedFunction(OpenApiOperationFunctionContext context) throws Exception;

	/**
	 * Invoked at the end of building the {@link Operation}. This allows finalising
	 * the {@link Operation}.
	 * 
	 * @param context {@link OpenApiOperationContext}.
	 * @throws Exception If fails to complete the {@link Operation}.
	 */
	void buildComplete(OpenApiOperationContext context) throws Exception;

}
