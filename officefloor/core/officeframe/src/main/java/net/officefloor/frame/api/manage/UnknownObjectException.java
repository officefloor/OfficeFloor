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

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Indicates an unknown {@link ManagedObject} was requested.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownObjectException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Initiate.
	 *
	 * @param bindingDetails Binding details of the unknown object.
	 */
	public UnknownObjectException(String bindingDetails) {
		super("Unknown " + ManagedObject.class.getSimpleName() + " by binding " + bindingDetails);
	}

}
