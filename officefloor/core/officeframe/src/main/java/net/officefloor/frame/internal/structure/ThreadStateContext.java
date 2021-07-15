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
 * Context for executing a {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadStateContext {

	/**
	 * Indicates if the {@link ThreadState} is safe on {@link Thread}.
	 * 
	 * @return <code>true</code> if {@link ThreadState} safe on {@link Thread}.
	 */
	boolean isThreadStateSafe();

	/**
	 * Indicates if the {@link ThreadState} requires {@link Thread} safety.
	 * 
	 * @return <code>true</code> if {@link ThreadState} requires {@link Thread}
	 *         safety.
	 */
	boolean isRequireThreadStateSafety();

	/**
	 * Flag the {@link ThreadState} requires {@link Thread} safety.
	 */
	void flagRequiresThreadStateSafety();

	/**
	 * Creates a {@link FunctionState} for the {@link FunctionLogic} under the
	 * current {@link ThreadState}.
	 * 
	 * @param logic               {@link FunctionLogic}.
	 * @param fallbackThreadState Fall back {@link ThreadState} should there be no
	 *                            current {@link ThreadState}.
	 * @return {@link FunctionState} for the {@link FunctionLogic}.
	 */
	FunctionState createFunction(FunctionLogic logic, ThreadState fallbackThreadState);

	/**
	 * Executes the {@link FunctionState} returning the next {@link FunctionState}
	 * to execute.
	 * 
	 * @param function {@link FunctionState} to be executed.
	 * @return Next {@link FunctionState} to be executed. May be <code>null</code>.
	 * @throws Throwable Possible failure in executing the {@link FunctionState}.
	 */
	FunctionState executeFunction(FunctionState function) throws Throwable;

	/**
	 * Obtains the {@link ManagedObjectContainer} for the
	 * {@link ManagedObjectIndex}.
	 * 
	 * @param index {@link ManagedObjectIndex}.
	 * @return {@link ManagedObjectContainer}. May be <code>null</code> if not yet
	 *         instantiated.
	 */
	ManagedObjectContainer getManagedObject(ManagedObjectIndex index);

}
