/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.compile.spi.office;

import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Dependency of an {@link OfficeTask} on a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ObjectDependency {

	/**
	 * Obtains the name of this {@link ObjectDependency}. This is typically the
	 * {@link TaskObjectType} name.
	 * 
	 * @return Name of this {@link ObjectDependency}.
	 */
	String getObjectDependencyName();

	/**
	 * <p>
	 * Obtains the {@link ManagedObject} that full fills the dependency.
	 * <p>
	 * Expected return types are:
	 * <ol>
	 * <li>{@link OfficeSectionManagedObject}</li>
	 * <li>{@link OfficeManagedObject}</li>
	 * <li>{@link OfficeObject}</li>
	 * <li><code>null</code> if not yet linked (or issue in linking)</li>
	 * </ol>
	 * 
	 * @return {@link DependentManagedObject} or <code>null</code> if not yet
	 *         linked (or issue in linking).
	 */
	DependentManagedObject getDependentManagedObject();
}