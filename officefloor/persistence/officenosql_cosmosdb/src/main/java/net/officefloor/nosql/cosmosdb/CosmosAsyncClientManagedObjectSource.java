package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for the {@link CosmosAsyncClient}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosAsyncClientManagedObjectSource extends AbstractManagedObjectSource<None, None>
		implements ManagedObject {

	/**
	 * {@link CosmosAsyncClient}.
	 */
	private CosmosAsyncClient client;

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
		context.setObjectClass(CosmosAsyncClient.class);

		// Nothing further, as have all type information
		if (mosContext.isLoadingType()) {
			return;
		}

		// Create the client
		CosmosClientBuilder builder = CosmosDbConnect.createCosmosClientBuilder(mosContext);
		this.client = builder.buildAsyncClient();
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