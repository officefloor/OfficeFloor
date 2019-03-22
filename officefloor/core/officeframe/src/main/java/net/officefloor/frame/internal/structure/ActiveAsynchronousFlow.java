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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.function.AsynchronousFlow;

/**
 * Actively executing {@link AsynchronousFlow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ActiveAsynchronousFlow extends LinkedListSetEntry<ActiveAsynchronousFlow, ManagedFunctionContainer> {

	/**
	 * Indicates if already waiting on completion.
	 * 
	 * @return <code>true</code> if already waiting on completion.
	 */
	boolean isWaiting();

	/**
	 * Ensure wait on completion.
	 * 
	 * @return {@link FunctionState} to wait on completion.
	 */
	FunctionState waitOnCompletion();

}