/*-
 * #%L
 * CosmosDB Connect
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosClientBuilder;

/**
 * Decorates the {@link CosmosClientBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CosmosClientBuilderDecorator {

	/**
	 * Decorates the {@link CosmosClientBuilder}.
	 * 
	 * @param builder {@link CosmosClientBuilder} to decorate.
	 * @return Decorated {@link CosmosClientBuilder}.
	 * @throws Exception If fails to decorate the {@link CosmosClientBuilder}.
	 */
	CosmosClientBuilder decorate(CosmosClientBuilder builder) throws Exception;

}
