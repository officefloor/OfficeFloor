package net.officefloor.jdbc.datasource;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Tests the {@link DefaultDataSourceFactory} for {@link DataSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultDataSourceFactoryTest extends AbstractDataSourceFactoryTestCase {

	@Override
	protected Class<? extends CommonDataSource> getDataSourceType() {
		return DataSource.class;
	}

	@Override
	protected CommonDataSource createCommonDataSource(SourceContext sourceContext) throws Exception {
		return new DefaultDataSourceFactory().createDataSource(sourceContext);
	}

}