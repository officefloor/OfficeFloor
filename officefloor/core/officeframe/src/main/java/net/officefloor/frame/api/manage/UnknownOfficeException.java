/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.api.manage;

/**
 * Indicates an unknown {@link Office} was requested.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownOfficeException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the unknown {@link Office}.
	 */
	private final String unknownOfficeName;

	/**
	 * Initiate.
	 * 
	 * @param unknownOfficeName Name of the unknown {@link Office}.
	 */
	public UnknownOfficeException(String unknownOfficeName) {
		super("Unknown Office '" + unknownOfficeName + "'");
		this.unknownOfficeName = unknownOfficeName;
	}

	/**
	 * Obtains the name of the unknown {@link Office}.
	 * 
	 * @return Name of the unknown {@link Office}.
	 */
	public String getUnknownOfficeName() {
		return this.unknownOfficeName;
	}
}
