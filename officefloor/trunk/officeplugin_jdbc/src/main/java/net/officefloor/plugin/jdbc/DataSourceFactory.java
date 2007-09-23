/*
 * Created on Jan 11, 2006
 */
package net.officefloor.plugin.jdbc;

import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.XADataSource;

/**
 * Creates a {@link javax.sql.ConnectionPoolDataSource}or a
 * {@link javax.sql.XADataSource}. Looking to pool the connections thus a
 * {@link javax.sql.DataSource}will not be created - wrap the
 * {@link javax.sql.DataSource}with a
 * {@link javax.sql.ConnectionPoolDataSource}if necessary.
 * 
 * @author Daniel
 */
public interface DataSourceFactory {

	/**
	 * Initialise with properties to create the data sources.
	 * 
	 * @param properties
	 *            Properties to configure the data sources.
	 * @throws Exception
	 *             If fails to initialise.
	 */
	void init(Properties properties) throws Exception;

	/**
	 * Creates a {@link ConnectionPoolDataSource}.
	 * 
	 * @return Configured {@link ConnectionPoolDataSource}.
	 * @throws Exception
	 *             If fail to create {@link ConnectionPoolDataSource}.
	 */
	ConnectionPoolDataSource createConnectionPoolDataSource() throws Exception;

	/**
	 * Creates a {@link XADataSource}.
	 * 
	 * @return Configured {@link XADataSource}.
	 * @throws Exception
	 *             If fail to create {@link XADataSource}.
	 */
	XADataSource createXADataSource() throws Exception;

	/**
	 * Obtains the timeout on connections created from the
	 * {@link javax.sql.DataSource}.
	 * 
	 * @return Timeout in milli-seconds of timeout on connections.
	 */
	long getConnectionTimeout();
}