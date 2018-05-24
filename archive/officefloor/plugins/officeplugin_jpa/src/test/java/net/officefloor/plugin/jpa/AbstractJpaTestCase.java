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
package net.officefloor.plugin.jpa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hsqldb.jdbcDriver;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Abstract functionality for the JPA testing.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractJpaTestCase extends OfficeFrameTestCase {

	/**
	 * {@link Connection}.
	 */
	protected Connection connection;

	/**
	 * {@link EntityManagerFactory}.
	 */
	private EntityManagerFactory factory;

	/**
	 * {@link OfficeFloor}.
	 */
	protected OfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {

		// Create the database
		Class.forName(jdbcDriver.class.getName());
		this.connection = DriverManager.getConnection("jdbc:hsqldb:mem:test", "sa", "");
		Statement statement = this.connection.createStatement();
		statement.execute(
				"CREATE TABLE MOCKENTITY ( ID IDENTITY PRIMARY KEY, NAME VARCHAR(20) NOT NULL, DESCRIPTION VARCHAR(256) )");
		statement.close();

		// Create the entity manager factory
		Properties properties = new Properties();
		properties.put("datanucleus.ConnectionDriverName", jdbcDriver.class.getName());
		this.factory = Persistence.createEntityManagerFactory("test", properties);
	}

	@Override
	protected void tearDown() throws Exception {

		// Close OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}

		try {
			// Stop factory
			this.factory.close();

		} finally {
			// Ensure destroy database
			if (this.connection != null) {
				Statement statement = this.connection.createStatement();
				statement.execute("SHUTDOWN IMMEDIATELY");
				this.connection.close();
			}
		}
	}

	/**
	 * Creates the {@link EntityManager} for testing.
	 * 
	 * @return {@link EntityManager} for testing.
	 */
	protected EntityManager createEntityManager() {
		return this.factory.createEntityManager();
	}

}