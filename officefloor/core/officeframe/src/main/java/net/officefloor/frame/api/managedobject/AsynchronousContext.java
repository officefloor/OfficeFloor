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
package net.officefloor.frame.api.managedobject;

/**
 * Context to be notified about asynchronous operations by the
 * {@link AsynchronousManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AsynchronousContext {

	/**
	 * Undertakes an {@link AsynchronousOperation}.
	 *
	 * @param <T>
	 *            Possible exception type from {@link AsynchronousOperation}.
	 * @param operation
	 *            Optional operation to be undertaken, once the
	 *            {@link AsynchronousManagedObject} is registered as started an
	 *            asynchronous operation. May be <code>null</code>.
	 */
	<T extends Throwable> void start(AsynchronousOperation<T> operation);

	/**
	 * Indicates that the {@link AsynchronousManagedObject} has completed and is
	 * ready for another operation.
	 * 
	 * @param <T>
	 *            Possible exception type from {@link AsynchronousOperation}.
	 * @param operation
	 *            Optional operation to be undertaken, once the
	 *            {@link AsynchronousManagedObject} is unregistered from undertaking
	 *            an asynchronous operation. May be <code>null</code>.
	 */
	<T extends Throwable> void complete(AsynchronousOperation<T> operation);

}