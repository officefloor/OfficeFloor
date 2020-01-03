package net.officefloor.jdbc.datasource;

import javax.sql.DataSource;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Factory for the creation of a {@link DataSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DataSourceFactory {

	/**
	 * Creates the {@link DataSource}.
	 * 
	 * @param context {@link SourceContext} to configure the {@link DataSource}.
	 * @return {@link DataSource}.
	 * @throws Exception If fails to create the {@link DataSource}.
	 */
	DataSource createDataSource(SourceContext context) throws Exception;

}