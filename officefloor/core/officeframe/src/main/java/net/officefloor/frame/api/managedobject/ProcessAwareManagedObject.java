/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.api.managedobject;

import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Enables the {@link ManagedObject} to undertake {@link ProcessSafeOperation}
 * instances.
 * <p>
 * This should be the preferred means to undertake any {@link ProcessState}
 * critical sections, as locks are only obtained if required. This,
 * subsequently, reduces {@link Thread} overheads and improves performance.
 *
 * @author Daniel Sagenschneider
 */
public interface ProcessAwareManagedObject extends ManagedObject {

	/**
	 * Provides the {@link ProcessAwareContext} to the {@link ManagedObject}.
	 * 
	 * @param context
	 *            {@link ProcessAwareContext}.
	 */
	void setProcessAwareContext(ProcessAwareContext context);

}