/*-
 * #%L
 * OpenAPI
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
