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
package net.officefloor.plugin.jdbc.hsqldb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceLoader;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.jdbc.JdbcManagedObject;
import net.officefloor.plugin.jdbc.JdbcManagedObjectSource;

/**
 * Tests the HSQLDB IMDB.
 * 
 * @author Daniel
 */
public class HsqldbImdbTest extends OfficeFrameTestCase {

	/**
	 * Ensures appropriately initialises the IMDB.
	 */
	public void testImdbViaDataSourceFactory() throws Exception {

		// Load the managed object source
		ManagedObjectSourceLoader loader = new ManagedObjectSourceLoader();
		loader.addProperty(
				JdbcManagedObjectSource.DATA_SOURCE_FACTORY_CLASS_PROPERTY,
				HsqldbImdbDataSourceFactory.class.getName());
		loader.addProperty(HsqldbImdbDataSourceFactory.HSQLDB_DATABASE_NAME,
				"Test");
		loader
				.addProperty(
						JdbcManagedObjectSource.DATA_SOURCE_INITIALISE_SCRIPT,
						this.getFileLocation(this.getClass(),
								"HsqldbImdbTest.sql"));
		JdbcManagedObjectSource managedObjectSource = loader
				.loadManagedObjectSource(JdbcManagedObjectSource.class);

		// Do the test
		this.doTest(managedObjectSource);
	}

	/**
	 * Ensures appropriately initialises the IMDB.
	 * 
	 * @throws Exception
	 */
	public void testImdbManagedObjectSource() throws Exception {

		// Load the managed object source
		ManagedObjectSourceLoader loader = new ManagedObjectSourceLoader();
		loader.addProperty(HsqldbImdbDataSourceFactory.HSQLDB_DATABASE_NAME,
				"Test");
		loader
				.addProperty(
						JdbcManagedObjectSource.DATA_SOURCE_INITIALISE_SCRIPT,
						this.getFileLocation(this.getClass(),
								"HsqldbImdbTest.sql"));
		JdbcManagedObjectSource managedObjectSource = loader
				.loadManagedObjectSource(ImdbManagedObjectSource.class);

		// DO the test
		this.doTest(managedObjectSource);
	}

	/**
	 * Does the test.
	 * 
	 * @param managedObjectSource
	 *            {@link JdbcManagedObjectSource} to provide the
	 *            {@link Connection}.
	 */
	protected void doTest(JdbcManagedObjectSource managedObjectSource)
			throws Exception {

		// Obtain the Jdbc Managed Object
		JdbcManagedObject managedObject = (JdbcManagedObject) ManagedObjectUserStandAlone
				.sourceManagedObject(managedObjectSource);

		// Obtain the connection
		Connection connection = (Connection) managedObject.getObject();

		// Ensure able to obtain the product name (from initialising)
		PreparedStatement statement = connection
				.prepareStatement("SELECT PRODUCT_NAME FROM PRODUCT WHERE PRODUCT_ID = 1");
		ResultSet resultSet = statement.executeQuery();
		assertTrue("Should have a resulting row", resultSet.next());
		assertEquals("Incorrect product name", "Test", resultSet
				.getString("PRODUCT_NAME"));

		// Shutdown the IMDB
		connection.createStatement().execute("SHUTDOWN");
		connection.close();
	}

}
