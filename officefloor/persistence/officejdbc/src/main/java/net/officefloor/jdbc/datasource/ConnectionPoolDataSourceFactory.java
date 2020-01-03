package net.officefloor.jdbc.datasource;

import javax.sql.ConnectionPoolDataSource;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Factory for the creation of a {@link ConnectionPoolDataSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConnectionPoolDataSourceFactory {

	/**
	 * Creates the {@link ConnectionPoolDataSource}.
	 * 
	 * @param context {@link SourceContext} to configure the
	 *                {@link ConnectionPoolDataSource}.
	 * @return {@link ConnectionPoolDataSource}.
	 * @throws Exception If fails to create the {@link ConnectionPoolDataSource}.
	 */
	ConnectionPoolDataSource createConnectionPoolDataSource(SourceContext context) throws Exception;

}