/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.plugin.jdbc;

import java.io.PrintWriter;
import java.sql.Driver;
import java.sql.SQLException;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.ConnectionPoolDataSource#getPooledConnection()
	 */
	@Override
	public PooledConnection getPooledConnection() throws SQLException {
		return pooledConnection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.sql.ConnectionPoolDataSource#getPooledConnection(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public PooledConnection getPooledConnection(String user, String password)
			throws SQLException {
		return pooledConnection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.CommonDataSource#getLogWriter()
	 */
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return this.logWriter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
	 */
	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		this.logWriter = out;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.CommonDataSource#getLoginTimeout()
	 */
	@Override
	public int getLoginTimeout() throws SQLException {
		return this.loginTimeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.CommonDataSource#setLoginTimeout(int)
	 */
	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		this.loginTimeout = seconds;
	}

}
