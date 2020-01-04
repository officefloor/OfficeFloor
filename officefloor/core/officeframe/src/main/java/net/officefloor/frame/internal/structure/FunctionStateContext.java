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

package net.officefloor.frame.internal.structure;

/**
 * Context for executing a {@link FunctionState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionStateContext {

	/**
	 * Wraps executing the delegate {@link FunctionState} to enable breaking the
	 * delegate chain. This avoids {@link StackOverflowError} issues.
	 * 
	 * @param delegate
	 *            Delegate {@link FunctionState}.
	 * @return Next {@link FunctionState} to execute.
	 * @throws Throwable
	 *             If failure in executing the delegate {@link FunctionState}.
	 */
	FunctionState executeDelegate(FunctionState delegate) throws Throwable;

}
