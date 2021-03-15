package net.officefloor.nosql.cosmosdb;

/**
 * Tests the {@link CosmosAsyncDbSupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosAsyncDbSupplierTest extends AbstractCosmosSupplierTestCase {

	@Override
	@SuppressWarnings("unchecked")
	protected Class<CosmosAsyncDbSupplierSource> getSupplierSourceClass() {
		return CosmosAsyncDbSupplierSource.class;
	}

	@Override
	protected boolean isAsynchronous() {
		return true;
	}

}