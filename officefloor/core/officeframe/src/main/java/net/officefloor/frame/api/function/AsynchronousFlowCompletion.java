/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.frame.api.function;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * <p>
 * Allows {@link ThreadState} safe logic to run on the completion of the
 * {@link AsynchronousFlow}.
 * <p>
 * As the {@link AsynchronousFlow} is very likely to use other {@link Thread}
 * instances (and likely call the completion of {@link AsynchronousFlow} on
 * another {@link Thread}), this allows {@link ThreadState} logic to synchronise
 * the results back into the {@link ManagedFunction} and its dependent
 * {@link ManagedObject} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface AsynchronousFlowCompletion {

	/**
	 * Contains the {@link ThreadState} safe logic.
	 * 
	 * @throws Throwable Indicate a failure in the {@link AsynchronousFlow}.
	 */
	void run() throws Throwable;

}