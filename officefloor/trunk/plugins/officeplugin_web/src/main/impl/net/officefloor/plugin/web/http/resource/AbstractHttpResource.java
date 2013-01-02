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
package net.officefloor.plugin.web.http.resource;

/**
 * Abstract {@link HttpResource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpResource implements HttpResource {

	/**
	 * Resource path.
	 */
	protected String resourcePath;

	/**
	 * Initiate.
	 * 
	 * @param resourcePath
	 *            Resource path.
	 */
	public AbstractHttpResource(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	/*
	 * ======================= HttpResource ==========================
	 */

	@Override
	public String getPath() {
		return this.resourcePath;
	}

	/*
	 * ========================= Object ===========================
	 */

	@Override
	public boolean equals(Object obj) {

		// Check if same object
		if (this == obj) {
			return true;
		}

		// Ensure same type
		if (!(obj instanceof AbstractHttpResource)) {
			return false;
		}
		AbstractHttpResource that = (AbstractHttpResource) obj;

		// Return whether same resource by path
		return this.resourcePath.equals(that.resourcePath);
	}

	@Override
	public int hashCode() {
		return this.resourcePath.hashCode();
	}

}