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
 * Indicates timed out waiting on the object.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectTimeoutException extends Exception {

	/**
	 * Default serialisation.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Bound object name.
	 */
	private final String boundObjectName;

	/**
	 * Instantiate.
	 * 
	 * @param boundObjectName     Bound object name.
	 * @param timeoutMilliseconds Time out in milliseconds.
	 */
	public ObjectTimeoutException(String boundObjectName, long timeoutMilliseconds) {
		super("Timed out waiting on object " + boundObjectName + " after " + timeoutMilliseconds + " milliseconds");
		this.boundObjectName = boundObjectName;
	}

	/**
	 * Obtains the bound object name.
	 * 
	 * @return Bound object name.
	 */
	public String getBoundObjectName() {
		return this.boundObjectName;
	}
}
