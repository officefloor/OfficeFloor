/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.spi.officefloor;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;

/**
 * Handles {@link CleanupEscalation} instances from other {@link ManagedObject}
 * instances used to service the input.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExternalServiceCleanupEscalationHandler<M extends ManagedObject> {

	/**
	 * Invoked to handle the {@link CleanupEscalation} instances.
	 * 
	 * @param inputManagedObject
	 *            Input {@link ManagedObject}.
	 * @param cleanupEscalations
	 *            {@link CleanupEscalation} instances.
	 * @throws Throwable
	 *             If fails to handle {@link CleanupEscalation} instances.
	 */
	void handleCleanupEscalations(M inputManagedObject, CleanupEscalation[] cleanupEscalations) throws Throwable;

}
