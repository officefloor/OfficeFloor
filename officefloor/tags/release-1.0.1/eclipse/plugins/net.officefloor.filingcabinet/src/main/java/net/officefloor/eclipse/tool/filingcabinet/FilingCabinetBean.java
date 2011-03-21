/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.plugin.filingcabinet.FilingCabinetGenerator;

/**
 * Bean to be populated with details for the {@link FilingCabinetGenerator}.
 * 
 * @author Daniel Sagenschneider
 */
public class FilingCabinetBean {

	/**
	 * Database driver.
	 */
	private String databaseDriver;

	public String getDatabaseDriver() {
		return this.databaseDriver;
	}

	public void setDatabaseDriver(String databaseDriver) {
		this.databaseDriver = databaseDriver;
	}

	/**
	 * URL of the database.
	 */
	private String databaseUrl;

	public String getDatabaseUrl() {
		return this.databaseUrl;
	}

	public void setDatabaseUrl(String databaseUrl) {
		this.databaseUrl = databaseUrl;
	}

	/**
	 * User name for the database.
	 */
	private String databaseUserName;

	public String getDatabaseUserName() {
		return this.databaseUserName;
	}

	public void setDatabaseUserName(String databaseUserName) {
		this.databaseUserName = databaseUserName;
	}

	/**
	 * Password for the database.
	 */
	private String databasePassword;

	public String getDatabasePassword() {
		return this.databasePassword;
	}

	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}

	/**
	 * Location to generate the files.
	 */
	private String location;

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}