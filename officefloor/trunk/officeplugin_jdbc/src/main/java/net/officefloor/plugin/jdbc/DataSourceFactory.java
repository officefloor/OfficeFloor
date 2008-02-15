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
	 * Creates a {@link ConnectionPoolDataSource}.
	 * 
	 * @param properties
	 *            Properties to configure the data source.
	 * @return Configured {@link ConnectionPoolDataSource}.
	 * @throws Exception
	 *             If fail to create {@link ConnectionPoolDataSource}.
	 */
	ConnectionPoolDataSource createConnectionPoolDataSource(
			Properties properties) throws Exception;

	/**
	 * Creates a {@link XADataSource}.
	 * 
	 * @param properties
	 *            Properties to configure the data source.
	 * @return Configured {@link XADataSource}.
	 * @throws Exception
	 *             If fail to create {@link XADataSource}.
	 */
	XADataSource createXADataSource(Properties properties) throws Exception;

}