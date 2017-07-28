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
/*
 * Created on Jan 12, 2006
 */
package net.officefloor.frame.api.managedobject;

/**
 * <p>
 * Provides the ability for the {@link ManagedObject} to obtain references to
 * the Objects of other {@link ManagedObject} instances.
 * <p>
 * Optionally implemented by the {@link ManagedObject} provider.
 * 
 * @author Daniel Sagenschneider
 */
public interface CoordinatingManagedObject<O extends Enum<O>> extends ManagedObject {

	/**
	 * <p>
	 * Loads the Objects of the {@link ManagedObject} instances to be referenced
	 * by this {@link CoordinatingManagedObject}.
	 * <p>
	 * References to the loaded Objects must be released on recycling the
	 * {@link ManagedObject}.
	 * 
	 * @param registry
	 *            Registry of the Objects for the {@link ManagedObject}
	 *            instances.
	 * @throws Throwable
	 *             Should this {@link CoordinatingManagedObject} fail to load
	 *             the {@link ManagedObject}.
	 */
	void loadObjects(ObjectRegistry<O> registry) throws Throwable;

}