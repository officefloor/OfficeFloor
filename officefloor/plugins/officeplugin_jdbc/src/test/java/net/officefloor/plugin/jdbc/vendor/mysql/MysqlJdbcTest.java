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
package net.officefloor.plugin.jdbc.vendor.mysql;

import java.util.Properties;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

import net.officefloor.plugin.jdbc.connection.JdbcManagedObjectSource;
import net.officefloor.plugin.jdbc.vendor.AbstractVendorJdbcTest;

/**
 * Tests MySQL.
 * 
 * @author Daniel Sagenschneider
 */
public class MysqlJdbcTest extends AbstractVendorJdbcTest {

	@Override
	protected void loadProperties(Properties properties) {
		properties
				.setProperty(
						JdbcManagedObjectSource.CONNECTION_POOL_DATA_SOURCE_CLASS_PROPERTY,
						MysqlConnectionPoolDataSource.class.getName());
		properties.setProperty("serverName", "localhost");
		properties.setProperty("port", "3306");
		properties.setProperty("databaseName", "officefloor");
		properties.setProperty("user", "officefloor");
		properties.setProperty("password", "password");
	}

}