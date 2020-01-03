package net.officefloor.jdbc.h2;

import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.datasource.ConnectionPoolDataSourceFactory;
import net.officefloor.jdbc.datasource.DataSourceFactory;

/**
 * H2 {@link ConnectionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class H2ConnectionManagedObjectSource extends ConnectionManagedObjectSource implements H2DataSourceFactory {

	/*
	 * ============= ConnectionManagedObjectSource ===========
	 */

	@Override
	public void loadSpecification(SpecificationContext context) {
		H2DataSourceFactory.loadSpecification(context);
	}

	@Override
	protected DataSourceFactory getDataSourceFactory(SourceContext context) {
		return this;
	}

	@Override
	protected ConnectionPoolDataSourceFactory getConnectionPoolDataSourceFactory(SourceContext context) {
		return this;
	}

}