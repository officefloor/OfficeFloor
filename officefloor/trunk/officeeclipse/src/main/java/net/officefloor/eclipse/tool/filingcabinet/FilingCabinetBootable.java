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
package net.officefloor.eclipse.tool.filingcabinet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

import net.officefloor.eclipse.bootstrap.Bootable;
import net.officefloor.plugin.filingcabinet.CommonDatabaseAwareness;
import net.officefloor.plugin.filingcabinet.FilingCabinetGenerator;

/**
 * Main for the {@link FilingCabinetGenerator}.
 * 
 * @author Daniel
 */
public class FilingCabinetBootable implements Bootable {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.bootstrap.Bootable#boot(java.util.Map)
	 */
	@Override
	public void boot(Map<String, String> arguments) throws Throwable {

		String driverClassName = arguments.get("driver");
		String url = arguments.get("url");
		String userName = arguments.get("username");
		String password = arguments.get("password");
		String packageName = arguments.get("package");

		// Load the database driver
		this.getClass().getClassLoader().loadClass(driverClassName)
				.newInstance();

		// Obtain the connection (ensuring driver available)
		Connection connection = DriverManager.getConnection(url, userName,
				password);

		// Generate the filing cabinet files
		FilingCabinetGenerator filingCabinetGenerator = new FilingCabinetGenerator(
				packageName);
		filingCabinetGenerator.loadMetaData(connection.getMetaData(),
				new CommonDatabaseAwareness());
	}

}
