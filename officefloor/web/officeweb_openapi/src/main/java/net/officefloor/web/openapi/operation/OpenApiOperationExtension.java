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
