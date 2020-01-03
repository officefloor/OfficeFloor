package net.officefloor.jdbc.h2;

import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.ReadOnlyConnectionManagedObjectSource;
import net.officefloor.jdbc.datasource.DataSourceFactory;

/**
 * H2 read-only {@link ConnectionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class H2ReadOnlyConnectionManagedObjectSource extends ReadOnlyConnectionManagedObjectSource
		implements H2DataSourceFactory {

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

}