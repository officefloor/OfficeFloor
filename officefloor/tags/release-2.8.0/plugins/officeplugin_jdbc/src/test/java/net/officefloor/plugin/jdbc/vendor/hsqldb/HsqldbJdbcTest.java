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
package net.officefloor.plugin.jdbc.vendor.hsqldb;

import java.util.Properties;

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.hsqldb.jdbcDriver;

import net.officefloor.plugin.jdbc.connection.JdbcManagedObjectSource;
import net.officefloor.plugin.jdbc.vendor.AbstractVendorJdbcTest;

/**
 * Tests HSQLDB with {@link JdbcManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HsqldbJdbcTest extends AbstractVendorJdbcTest {

	@Override
	protected void loadProperties(Properties properties) {
		properties
				.setProperty(
						JdbcManagedObjectSource.CONNECTION_POOL_DATA_SOURCE_CLASS_PROPERTY,
						DriverAdapterCPDS.class.getName());
		properties.setProperty("driver", jdbcDriver.class.getName());
		properties.setProperty("url", "jdbc:hsqldb:mem:test");
		properties.setProperty("user", "sa");
		properties.setProperty("password", "");
	}

	@Override
	protected boolean isDropTablesOnSetup() {
		return false; // IMDB always fresh on startup
	}

}