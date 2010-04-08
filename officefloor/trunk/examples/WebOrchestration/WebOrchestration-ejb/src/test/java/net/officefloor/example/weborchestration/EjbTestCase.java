/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.example.weborchestration;

import java.util.Properties;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.TestCase;

import net.officefloor.example.weborchestration.TestResetLocal;

import org.apache.openejb.client.LocalInitialContextFactory;
import org.hsqldb.jdbcDriver;

/**
 * Abstract {@link TestCase} for testing the EJB instances.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class EjbTestCase extends TestCase {

	/**
	 * Obtains the {@link Context} for testing.
	 * 
	 * @return {@link Context} for testing.
	 */
	public static Context getTestContext() {
		try {
			// Create the properties for the test context
			Properties properties = new Properties();
			properties.setProperty(Context.INITIAL_CONTEXT_FACTORY,
					LocalInitialContextFactory.class.getName());

			// DataSource
			properties.setProperty("productDatabase",
					"new://Resource?type=DataSource");
			properties.setProperty("productDatabase.jdbcDriver",
					jdbcDriver.class.getName());
			properties.setProperty("productDatabase.jdbcUrl",
					"jdbc:hsqldb:mem:productdb");

			// Obtain the context
			Context context = new InitialContext(properties);

			// Return the context
			return context;

		} catch (NamingException ex) {
			ex.printStackTrace();
			TestCase.fail("Failed creating InitialContext: " + ex.getMessage());
			return null;
		}
	}

	/**
	 * {@link Context} for testing.
	 */
	protected final Context context = EjbTestCase.getTestContext();

	/**
	 * Resets the {@link Context} for next test.
	 * 
	 * @param context
	 *            {@link Context}.
	 */
	public void resetContext() {
		this.lookup(TestResetLocal.class).reset();
	}

	@Override
	protected void setUp() throws Exception {
		this.resetContext();
	}

	/**
	 * Looks up the EJB.
	 * 
	 * @param context
	 *            {@link Context}.
	 * @param type
	 *            {@link Local} or {@link Remote} EJB interface should EJB
	 *            implementation follow naming convention.
	 * @return {@link Local} or {@link Remote} EJB.
	 */
	@SuppressWarnings("unchecked")
	public <T> T lookup(Class<T> type) {
		try {
			return (T) this.context.lookup(type.getSimpleName());
		} catch (NamingException ex) {
			ex.printStackTrace();
			TestCase.fail("Failed looking up " + type.getSimpleName() + ": "
					+ ex.getMessage());
			return null;
		}
	}

}