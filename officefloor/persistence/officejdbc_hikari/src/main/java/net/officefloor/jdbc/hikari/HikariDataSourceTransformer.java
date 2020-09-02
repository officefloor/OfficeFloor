package net.officefloor.jdbc.hikari;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.jdbc.datasource.DataSourceTransformer;
import net.officefloor.jdbc.datasource.DataSourceTransformerContext;
import net.officefloor.jdbc.datasource.DataSourceTransformerServiceFactory;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;

/**
 * Wraps the {@link DataSource} with {@link HikariDataSource} for pooling.
 * 
 * @author Daniel Sagenschneider
 */
public class HikariDataSourceTransformer implements DataSourceTransformer, DataSourceTransformerServiceFactory {

	/*
	 * ================== DataSourceTransformerServiceFactory =============
	 */

	@Override
	public DataSourceTransformer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================== DataSourceTransformer ======================
	 */

	@Override
	public DataSource transformDataSource(DataSourceTransformerContext context) throws Exception {

		// Create the Hikari DataSource
		HikariDataSource hikari = new HikariDataSource();
		hikari.setDataSource(context.getDataSource());

		// Load properties to configure pooling
		DefaultDataSourceFactory.loadProperties(hikari, context.getSourceContext());

		// Return the Hikari
		return hikari;
	}

}