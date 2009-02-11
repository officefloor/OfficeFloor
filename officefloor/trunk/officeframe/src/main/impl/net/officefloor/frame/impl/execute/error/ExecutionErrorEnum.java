/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.execute.error;

/**
 * Types of execution errors that may occur.
 * 
 * @author Daniel
 */
public enum ExecutionErrorEnum {

	/**
	 * Indicates failed to source the {@link ManagedObject} from its
	 * {@link ManagedObjectSource}.
	 */
	MANAGED_OBJECT_SOURCING_FAILURE,

	/**
	 * Indicates the {@link ManagedObject} was not loaded in attempting to
	 * access it.
	 */
	MANAGED_OBJECT_NOT_LOADED,

	/**
	 * Indicates the {@link ManagedObject} failed to provide the Object it is
	 * managing.
	 */
	MANAGED_OBJECT_FAILED_PROVIDING_OBJECT,

	/**
	 * Indicates the asynchronous operation by the {@link ManagedObject} has
	 * timed out.
	 */
	MANAGED_OBJECT_ASYNC_OPERATION_TIMED_OUT
}
