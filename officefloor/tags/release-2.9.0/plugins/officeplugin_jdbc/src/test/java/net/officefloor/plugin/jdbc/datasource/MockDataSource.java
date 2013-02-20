/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.jdbc.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import junit.framework.TestCase;

/**
 * Mock {@link DataSource} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockDataSource implements DataSource {

	/**
	 * {@link Connection} to return.
	 */
	private static Connection connection;

	/**
	 * Last instantiated instance.
	 */
	private static MockDataSource instance;

	/**
	 * Specifies the {@link Connection} to return from this {@link DataSource}.
	 * 
	 * @param connection
	 *            {@link Connection} to return from this {@link DataSource}.
	 */
	public static void setConnection(Connection connection) {
		MockDataSource.connection = connection;
	}

	/**
	 * Obtains the last instantiated instance.
	 * 
	 * @return Last instantiated instance.
	 */
	public static MockDataSource getInstance() {
		return instance;
	}

	/**
	 * Initiate.
	 */
	public MockDataSource() {
		instance = this;
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
	private int port;

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
	 * =================== DataSource ============================
	 */

	@Override
	public Connection getConnection() throws SQLException {
		return connection;
	}

	@Override
	public Connection getConnection(String username, String password)
			throws SQLException {
		TestCase.fail("Should not be invoked");
		return null;
	}

	// New to Java 7 (not override to allow compatibility with Java 6)
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		TestCase.fail("Should not be invoked");
		return null;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		TestCase.fail("Should not be invoked");
		return null;
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		TestCase.fail("Should not be invoked");
		return -1;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		TestCase.fail("Should not be invoked");
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		TestCase.fail("Should not be invoked");
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		TestCase.fail("Should not be invoked");
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		TestCase.fail("Should not be invoked");
		return null;
	}

}