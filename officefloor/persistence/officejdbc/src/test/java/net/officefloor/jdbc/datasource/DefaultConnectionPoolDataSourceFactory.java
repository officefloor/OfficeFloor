package net.officefloor.jdbc.datasource;

import javax.sql.CommonDataSource;
import javax.sql.ConnectionPoolDataSource;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Tests the {@link DefaultDataSourceFactory} for the
 * {@link ConnectionPoolDataSourceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultConnectionPoolDataSourceFactory extends AbstractDataSourceFactoryTestCase {

	@Override
	protected Class<? extends CommonDataSource> getDataSourceType() {
		return ConnectionPoolDataSource.class;
	}

	@Override
	protected CommonDataSource createCommonDataSource(SourceContext sourceContext) throws Exception {
		return new DefaultDataSourceFactory().createConnectionPoolDataSource(sourceContext);
	}

}