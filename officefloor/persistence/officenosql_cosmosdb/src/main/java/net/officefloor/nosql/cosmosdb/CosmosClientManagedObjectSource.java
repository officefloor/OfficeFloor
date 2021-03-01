package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectService;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;

/**
 * {@link ManagedObjectSource} for the {@link CosmosClient}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosClientManagedObjectSource extends AbstractManagedObjectSource<None, None> implements ManagedObject {

	/**
	 * {@link SourceContext}.
	 */
	private SourceContext sourceContext;

	/**
	 * {@link CosmosClient}.
	 */
	private CosmosClient client;

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

		// Capture the source context for service start
		this.sourceContext = context.getManagedObjectSourceContext();

		// Load the meta-data
		context.setObjectClass(CosmosClient.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<None> context) throws Exception {

		context.addService(new ManagedObjectService<None>() {

			@Override
			public void startServicing(ManagedObjectServiceContext<None> serviceContext) throws Exception {

				// Easy access
				CosmosClientManagedObjectSource source = CosmosClientManagedObjectSource.this;

				// Create the client
				CosmosClientBuilder builder = CosmosDbConnect.createCosmosClientBuilder(source.sourceContext);
				source.client = builder.buildClient();
			}

			@Override
			public void stopServicing() {

				// Easy access
				CosmosClientManagedObjectSource source = CosmosClientManagedObjectSource.this;

				// Stop connection
				if (source.client != null) {
					source.client.close();
				}
			}
		});
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
