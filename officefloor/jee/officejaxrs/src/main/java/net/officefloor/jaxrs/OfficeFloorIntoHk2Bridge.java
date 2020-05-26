/*-
 * #%L
 * JAX-RS
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

package net.officefloor.jaxrs;

import org.jvnet.hk2.annotations.Contract;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * This enables {@link OfficeFloor} to provide {@link ManagedObject} instances
 * to HK2.
 * 
 * @author Daniel Sagenschneider
 */
@Contract
public interface OfficeFloorIntoHk2Bridge {

	/**
	 * Bridges {@link OfficeFloor} into HK2.
	 * 
	 * @param dependencies {@link OfficeFloorDependencies}.
	 */
	void bridgeOfficeFloor(OfficeFloorDependencies dependencies);
}
