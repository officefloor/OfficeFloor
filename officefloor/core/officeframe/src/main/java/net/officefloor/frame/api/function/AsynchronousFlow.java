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

package net.officefloor.frame.api.function;

import net.officefloor.frame.api.escalate.AsynchronousFlowTimedOutEscalation;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <p>
 * Provides an unmanaged asynchronous flow outside the {@link ManagedFunction}.
 * <p>
 * This allows plugging in other asynchronous libraries to enable asynchronous
 * operations within a {@link ManagedFunction}. This can include running
 * asynchronous operations on different {@link Thread} instances of the third
 * party library.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface AsynchronousFlow {

	/**
	 * <p>
	 * Invoked by application code once the {@link AsynchronousFlow} is complete.
	 * <p>
	 * Note that only the first invocation of this method is considered. All further
	 * invocations are ignored.
	 * <p>
	 * Furthermore, if the {@link AsynchronousFlow} takes too long to complete then
	 * all invocations are ignored. The {@link ManagedFunction} will be throwing an
	 * {@link AsynchronousFlowTimedOutEscalation}.
	 * 
	 * @param completion {@link AsynchronousFlowCompletion} to update
	 *                   {@link ManagedObject} state and possibly throw failures.
	 */
	void complete(AsynchronousFlowCompletion completion);

}
