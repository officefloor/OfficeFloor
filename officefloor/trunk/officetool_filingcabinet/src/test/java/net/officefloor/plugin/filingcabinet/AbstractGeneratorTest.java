/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.filingcabinet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.hsqldb.jdbcDriver;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Provides the abstract generator test functionality.
 * 
 * @author Daniel
 */
public abstract class AbstractGeneratorTest extends OfficeFrameTestCase {

	/**
	 * {@link Connection}.
	 */
	protected Connection connection;

	/**
	 * {@link FilingCabinetGenerator} being tested.
	 */
	protected FilingCabinetGenerator generator;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {

		String databaseName = "test";

		// Load driver and create database instance
		Class.forName(jdbcDriver.class.getName()).newInstance();
		this.connection = DriverManager.getConnection("jdbc:hsqldb:mem:"
				+ databaseName, "sa", "");

		// Load the database schema
		File schemaFile = this.findFile(this.getClass(), "DatabaseSchema.sql");
		BufferedReader reader = new BufferedReader(new FileReader(schemaFile));
		List<String> statements = new LinkedList<String>();
		StringBuilder currentStatement = new StringBuilder();
		String line;
		do {
			// Obtain the line
			line = reader.readLine();

			// Add line to current statement
			if (line != null) {
				currentStatement.append(line);
				currentStatement.append("\n");
			}

			// Determine if statement complete
			if ((line == null) || (line.trim().endsWith(";"))) {
				// Statement complete
				String statementText = currentStatement.toString();
				if (statementText.trim().length() > 0) {
					// Add the statement
					statements.add(statementText);
				}

				// Reset the statement for next
				currentStatement = new StringBuilder();
			}

		} while (line != null);

		// Run the statements
		for (String sql : statements) {
			Statement statement = this.connection.createStatement();
			statement.execute(sql);
			statement.close();
		}

		// Obtain the database meta-data
		DatabaseMetaData databaseMetaData = this.connection.getMetaData();

		// Create the database awareness
		DatabaseAwareness databaseAwareness = new CommonDatabaseAwareness();

		// Load the meta data
		this.generator = new FilingCabinetGenerator();
		this.generator.loadMetaData(databaseMetaData, databaseAwareness);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		// Shutdown database
		this.connection.createStatement().execute("SHUTDOWN");
		this.connection.close();
	}

}
