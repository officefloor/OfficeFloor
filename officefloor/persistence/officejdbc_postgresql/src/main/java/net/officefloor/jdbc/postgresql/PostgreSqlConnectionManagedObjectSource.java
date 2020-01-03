package net.officefloor.jdbc.postgresql;

import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.datasource.ConnectionPoolDataSourceFactory;
import net.officefloor.jdbc.datasource.DataSourceFactory;

/**
 * PostgreSQL {@link ConnectionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class PostgreSqlConnectionManagedObjectSource extends ConnectionManagedObjectSource
		implements PostgreSqlDataSourceFactory {

	/*
	 * =============== ConnectionManagedObjectSource =================
	 */

	@Override
	public void loadSpecification(SpecificationContext context) {
		PostgreSqlDataSourceFactory.loadSpecification(context);
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