/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.eclipse.tool.filingcabinet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

import net.officefloor.eclipse.bootstrap.Bootable;
import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.plugin.filingcabinet.CommonDatabaseAwareness;
import net.officefloor.plugin.filingcabinet.FilingCabinetGenerator;
import net.officefloor.plugin.filingcabinet.TableGenerator;
import net.officefloor.plugin.filingcabinet.TableMetaData;

/**
 * Main for the {@link FilingCabinetGenerator}.
 * 
 * @author Daniel Sagenschneider
 */
public class FilingCabinetBootable implements Bootable {

	@Override
	public void boot(Map<String, String> arguments) throws Throwable {

		String driverClassName = arguments.get("driver");
		String url = arguments.get("url");
		String userName = arguments.get("username");
		String password = arguments.get("password");
		String packageName = arguments.get("package");

		// Ensure have the location
		File location = new File(arguments.get("location"));
		if (!location.isDirectory()) {
			throw new FileNotFoundException("Can not find directory "
					+ location);
		}

		// Ensure the directory is writable
		if (!location.canWrite()) {
			throw new IOException("Can not write to directory " + location);
		}

		// Load the database driver
		this.getClass().getClassLoader().loadClass(driverClassName)
				.newInstance();

		// Obtain the connection (ensuring driver available)
		Connection connection = DriverManager.getConnection(url, userName,
				password);

		// Create the configuration context to write the files
		ConfigurationContext configurationContext = new FileSystemConfigurationContext(
				location);

		// Generate the filing cabinet files
		FilingCabinetGenerator filingCabinetGenerator = new FilingCabinetGenerator(
				packageName);
		filingCabinetGenerator.loadMetaData(connection.getMetaData(),
				new CommonDatabaseAwareness());
		for (TableMetaData table : filingCabinetGenerator.getTableMetaData()) {
			new TableGenerator(table).generate(configurationContext);
		}
	}
}