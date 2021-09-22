/*-
 * #%L
 * CosmosDB
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosClient;

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
		this.client = CosmosDbConnect.createCosmosClient(sourceContext);
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
