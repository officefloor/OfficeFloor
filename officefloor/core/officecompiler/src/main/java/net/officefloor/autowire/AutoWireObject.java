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
package net.officefloor.autowire;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Object for configuring auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireObject extends AutoWireProperties {

	/**
	 * <p>
	 * Obtains the {@link ManagedObjectSource} class.
	 * <p>
	 * May be an alias.
	 * 
	 * @return {@link ManagedObjectSource} class.
	 */
	String getManagedObjectSourceClassName();

	/**
	 * Obtains the {@link ManagedObjectSourceWirer}.
	 * 
	 * @return {@link ManagedObjectSourceWirer}.
	 */
	ManagedObjectSourceWirer getManagedObjectSourceWirer();

	/**
	 * Obtains the {@link AutoWire} instances that the
	 * {@link ManagedObjectSource} is to provide auto-wiring.
	 * 
	 * @return {@link AutoWire} instances that the {@link ManagedObjectSource}
	 *         is to provide auto-wiring.
	 */
	AutoWire[] getAutoWiring();

	/**
	 * Obtains the time-out for sourcing the {@link ManagedObject} from the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return Time-out for sourcing the {@link ManagedObject} from the
	 *         {@link ManagedObjectSource}.
	 */
	long getTimeout();

	/**
	 * Specifies the time-out for sourcing the {@link ManagedObject} from the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param timeout
	 *            Time-out for sourcing the {@link ManagedObject} from the
	 *            {@link ManagedObjectSource}.
	 */
	void setTimeout(long timeout);

}