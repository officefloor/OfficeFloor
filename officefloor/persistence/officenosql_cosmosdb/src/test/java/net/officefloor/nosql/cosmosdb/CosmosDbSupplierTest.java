package net.officefloor.nosql.cosmosdb;

/**
 * Tests the {@link AbstractCosmosDbSupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDbSupplierTest extends AbstractCosmosSupplierTestCase {

	@Override
	@SuppressWarnings("unchecked")
	protected Class<CosmosDbSupplierSource> getSupplierSourceClass() {
		return CosmosDbSupplierSource.class;
	}

	@Override
	protected boolean isAsynchronous() {
		return false;
	}

}