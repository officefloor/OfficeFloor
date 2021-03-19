/*-
 * #%L
 * CosmosDB
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

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;

/**
 * {@link ManagedObjectSource} for the {@link CosmosClient}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosClientManagedObjectSource extends AbstractManagedObjectSource<None, None> implements ManagedObject {

	/**
	 * {@link CosmosClient}.
	 */
	private CosmosClient client;

	/**
	 * Creates the {@link CosmosClient}.
	 * 
	 * @param sourceContext {@link SourceContext}.
	 * @return {@link CosmosClient}.
	 * @throws Exception If fails to create {@link CosmosClient}.
	 */
	public CosmosClient createCosmosClient(SourceContext sourceContext) throws Exception {
		CosmosClientBuilder builder = CosmosDbConnect.createCosmosClientBuilder(sourceContext);
		this.client = builder.buildClient();
		return this.client;
	}

	/*
	 * ====================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CosmosDbConnect.PROPERTY_URL, "URL");
		context.addProperty(CosmosDbConnect.PROPERTY_KEY, "Key");
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Load the meta-data
		context.setObjectClass(CosmosClient.class);

		// Nothing further, as have all type information
		if (mosContext.isLoadingType()) {
			return;
		}

		// Supplier setup
		if (this.client != null) {
			return;
		}

		// Create the client
		this.createCosmosClient(mosContext);
	}

	@Override
	public void stop() {

		// Stop connection
		if (this.client != null) {
			this.client.close();
		}
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ========================= ManagedObject =============================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.client;
	}

}
