/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package org.postgresql.jdbc;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.postgresql.PGProperty;
import org.postgresql.core.ConnectionFactory;
import org.postgresql.core.Field;
import org.postgresql.core.Query;
import org.postgresql.core.QueryExecutor;
import org.postgresql.core.ResultCursor;
import org.postgresql.core.v3.QueryExecutorImpl;
import org.postgresql.util.HostSpec;

/**
 * Access to PostgreSql internal classes.
 * 
 * @author Daniel Sagenschneider
 */
public class PostgreSqlAccess {

	/**
	 * {@link PgConnection}.
	 */
	private final PgConnection connection;

	/**
	 * {@link PgStatement}.
	 */
	private final PgStatement statement;

	/**
	 * Instantiate.
	 * 
	 * @param connection {@link PgConnection}.
	 * @throws SQLException If fails to setup access.
	 */
	public PostgreSqlAccess(PgConnection connection) throws SQLException {
		this.connection = connection;
		this.statement = (PgStatement) connection.createStatement();
	}

	/**
	 * Creates the {@link QueryExecutor}.
	 * 
	 * @param hostName Host name.
	 * @param port     Port.
	 * @param database Database name.
	 * @param username User name.
	 * @param password Password.
	 * @return {@link QueryExecutor}.
	 * @throws SQLException If fails to create {@link QueryExecutor}.
	 * @throws IOException  If fails to create {@link QueryExecutor}.
	 */
	public QueryExecutor createQueryExecutor(String hostName, int port, String database, String username,
			String password) throws SQLException, IOException {

		// Create the query executor
		HostSpec[] hostSpecs = new HostSpec[] { new HostSpec(hostName, port) };
		Properties info = new Properties();
		info.setProperty(PGProperty.PASSWORD.getName(), password);
		QueryExecutorImpl executor = (QueryExecutorImpl) ConnectionFactory.openConnection(hostSpecs, database, username,
				info);
		
		// Return the query executor
		return new PipelineQueryExecutor(executor, info);
	}

	/**
	 * Creates the {@link PgResultSet}.
	 * 
	 * @param originalQuery {@link Query}.
	 * @param fields
	 * @param tuples
	 * @param cursor
	 * @param maxRows
	 * @return
	 * @throws SQLException
	 */
	public PgResultSet createResultSet(Query originalQuery, Field[] fields, List<byte[][]> tuples, ResultCursor cursor,
			int maxRows) throws SQLException {
		return new PgResultSet(originalQuery, this.statement, fields, tuples, cursor, maxRows,
				this.statement.getMaxFieldSize(), this.statement.getResultSetType(),
				this.statement.getResultSetConcurrency(), this.statement.getResultSetHoldability());
	}

}