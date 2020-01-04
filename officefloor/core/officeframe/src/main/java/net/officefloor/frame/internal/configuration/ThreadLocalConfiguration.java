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

package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.thread.OptionalThreadLocal;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;

/**
 * Provides configuration for the {@link OptionalThreadLocal}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadLocalConfiguration {

	/**
	 * Specifies the {@link ManagedObjectIndex} identifying the
	 * {@link ManagedObject} for the {@link OptionalThreadLocal}.
	 * 
	 * @param managedObjectIndex {@link ManagedObjectIndex} identifying the
	 *                           {@link ManagedObject} for the
	 *                           {@link OptionalThreadLocal}.
	 */
	void setManagedObjectIndex(ManagedObjectIndex managedObjectIndex);

}
