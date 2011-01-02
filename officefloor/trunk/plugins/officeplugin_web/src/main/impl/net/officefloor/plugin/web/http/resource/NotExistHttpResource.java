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
package net.officefloor.plugin.web.http.resource;

import net.officefloor.plugin.web.http.resource.HttpResource;

/**
 * Not existing {@link HttpResource}.
 * 
 * @author Daniel Sagenschneider
 */
public class NotExistHttpResource implements HttpResource {

	/**
	 * Path.
	 */
	private final String path;

	/**
	 * Initiate.
	 * 
	 * @param path
	 *            Path.
	 */
	public NotExistHttpResource(String path) {
		this.path = path;
	}

	/*
	 * ====================== HttpResource ======================
	 */

	@Override
	public String getPath() {
		return this.path;
	}

	@Override
	public boolean isExist() {
		// Not exist
		return false;
	}

}