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
package net.officefloor.plugin.filingcabinet;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;

import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * Main for invoking the {@link FilingCabinetGenerator}.
 * 
 * @author Daniel Sagenschneider
 */
public class FilingCabinetMain {

	/**
	 * Main for invoking the {@link FilingCabinetGenerator}.
	 * 
	 * @param args
	 *            Command line arguments.
	 * @throws Exception
	 *             If fails to generate.
	 */
	public static void main(String[] args) throws Exception {

		// Obtain the arguments to generate sources
		if (args.length < 5) {
			System.err
					.println("USAGE java ... "
							+ FilingCabinetMain.class.getName()
							+ " <driver> <url> <username> <password> <package> [<location>]");
			System.exit(1);
		}
		String driverClassName = args[0];
		String databaseUrl = args[1];
		String databaseUserName = args[2];
		String databasePassword = args[3];
		String packagePrefix = args[4];

		// Obtain location (default to current directory)
		File location = (args.length < 6 ? new File(".") : new File(args[5]));
		if (!location.isDirectory()) {
			throw new FileNotFoundException("Can not find directory "
					+ location);
		}

		// Ensure driver available
		FilingCabinetGenerator.class.getClassLoader()
				.loadClass(driverClassName).newInstance();

		// Obtain the connection to the database
		Connection connection = DriverManager.getConnection(databaseUrl,
				databaseUserName, databasePassword);

		// Load the meta data from the database
		FilingCabinetGenerator filingCabinetGenerator = new FilingCabinetGenerator(
				packagePrefix);
		filingCabinetGenerator.loadMetaData(connection.getMetaData(),
				new CommonDatabaseAwareness());

		// Create configuration context to write the files
		ConfigurationContext context = new FileSystemConfigurationContext(
				location);

		// Write the files
		for (TableMetaData table : filingCabinetGenerator.getTableMetaData()) {
			TableGenerator tableGenerator = new TableGenerator(table);
			tableGenerator.generate(context);
		}
	}

}
