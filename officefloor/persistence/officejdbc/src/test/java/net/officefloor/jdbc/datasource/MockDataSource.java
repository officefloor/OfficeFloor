/*-
 * #%L
 * JDBC Persistence
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.jdbc.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.CommonDataSource;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;

import org.junit.Assert;

/**
 * Mock {@link DataSource} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockDataSource implements DataSource, ConnectionPoolDataSource {

	/**
	 * Asserts the configuration.
	 * 
	 * @param dataSource   {@link CommonDataSource}.
	 * @param driver       {@link Driver} class name.
	 * @param url          URL to the database.
	 * @param serverName   Name of the database server.
	 * @param port         Port to connect to the database.
	 * @param databaseName Name of the database.
	 * @param userName     User name.
	 * @param password     Password.
	 */
	public static void assertConfiguration(CommonDataSource dataSource, String driver, String url, String serverName,
			int port, String databaseName, String userName, String password) {

		// Ensure mock data source
		Assert.assertNotNull("No mock data source", dataSource);
		Assert.assertTrue("Should be mock data source", dataSource instanceof MockDataSource);
		MockDataSource mock = (MockDataSource) dataSource;

		// Ensure configuration is as expected
		Assert.assertEquals("Incorrect driver", driver, mock.getDriver());
		Assert.assertEquals("Incorrect url", url, mock.getUrl());
		Assert.assertEquals("Incorrect server", serverName, mock.getServerName());
		Assert.assertEquals("Incorrect port", port, mock.getPort());
		Assert.assertEquals("Incorrect database", databaseName, mock.getDatabaseName());
		Assert.assertEquals("Incorrect userName", userName, mock.getUsername());
		Assert.assertEquals("Incorrect password", password, mock.getPassword());
	}

	/*
	 * ============ Configuration methods to configure ==================
	 */

	/**
	 * {@link Driver} class name.
	 */
	private String driver;

	public String getDriver() {
		return this.driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	/**
	 * URL to the database.
	 */
	private String url;

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Name of the database server.
	 */
	private String serverName;

	public String getServerName() {
		return this.serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	/**
	 * Port on the database server to connection.
	 */
	private int port = -1;

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Name of the database on the server to use.
	 */
	private String databaseName;

	public String getDatabaseName() {
		return this.databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	/**
	 * Username to authenticate connection.
	 */
	private String username;

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Password to authenticate connection.
	 */
	private String password;

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/*
	 * ============ DataSource and ConnectionPoolDataSource ================
	 */

	@Override
	public Connection getConnection() throws SQLException {
		Assert.fail("Should not be invoked");
		return null;
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		Assert.fail("Should not be invoked");
		return null;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		Assert.fail("Should not be invoked");
		return null;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		Assert.fail("Should not be invoked");
		return null;
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		Assert.fail("Should not be invoked");
		return -1;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		Assert.fail("Should not be invoked");
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		Assert.fail("Should not be invoked");
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		Assert.fail("Should not be invoked");
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		Assert.fail("Should not be invoked");
		return null;
	}

	@Override
	public PooledConnection getPooledConnection() throws SQLException {
		Assert.fail("Should not be invoked");
		return null;
	}

	@Override
	public PooledConnection getPooledConnection(String user, String password) throws SQLException {
		Assert.fail("Should not be invoked");
		return null;
	}

}
