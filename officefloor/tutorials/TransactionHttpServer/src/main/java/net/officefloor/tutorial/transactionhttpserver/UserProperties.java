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
package net.officefloor.tutorial.transactionhttpserver;

import java.io.Serializable;

import net.officefloor.plugin.web.http.application.HttpParameters;

/**
 * Properties of the user.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@HttpParameters
public class UserProperties implements Serializable {

	private String userName;

	private String fullName;

	public UserProperties() {
	}

	public UserProperties(String userName, String fullName) {
		this.userName = userName;
		this.fullName = fullName;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFullName() {
		return this.fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

}
// END SNIPPET: tutorial
