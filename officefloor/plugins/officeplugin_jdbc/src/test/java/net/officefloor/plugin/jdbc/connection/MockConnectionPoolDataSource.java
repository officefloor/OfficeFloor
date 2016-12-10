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
package net.officefloor.plugin.jdbc.connection;

import java.io.PrintWriter;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

/**
 * Mock {@link ConnectionPoolDataSource} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockConnectionPoolDataSource implements ConnectionPoolDataSource {

	/**
	 * Instance of this created.
	 */
	private static MockConnectionPoolDataSource instance = null;

	/**
	 * {@link PooledConnection} to be returned.
	 */
	private static PooledConnection pooledConnection = null;

	/**
	 * Returns the created instance.
	 * 
	 * @return Created instance.
	 */
	public static MockConnectionPoolDataSource getInstance() {
		return instance;
	}

	/**
	 * Specifies the {@link PooledConnection} to be returned.
	 * 
	 * @param pooledConnection
	 *            {@link PooledConnection}.
	 */
	public static void setPooledConnection(PooledConnection pooledConnection) {
		MockConnectionPoolDataSource.pooledConnection = pooledConnection;
	}

	/**
	 * Initiate to specify the instance.
	 */
	public MockConnectionPoolDataSource() {
		instance = this;
	}

	/**
	 * Log {@link PrintWriter}.
	 */
	private PrintWriter logWriter = null;

	/**
	 * Login timeout.
	 */
	private int loginTimeout = 15; // 15 seconds

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
	 * ================= ConnectionPoolDataSource =======================
	 */

	@Override
	public PooledConnection getPooledConnection() throws SQLException {
		return pooledConnection;
	}

	@Override
	public PooledConnection getPooledConnection(String user, String password)
			throws SQLException {
		return pooledConnection;
	}

	// New to Java 7 (not override to allow compatibility with Java 6)
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException(
				"Should not be required for testing");
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return this.logWriter;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		this.logWriter = out;
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return this.loginTimeout;
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		this.loginTimeout = seconds;
	}

}
