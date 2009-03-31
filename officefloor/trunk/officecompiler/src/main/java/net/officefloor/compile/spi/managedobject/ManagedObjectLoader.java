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
package net.officefloor.compile.spi.managedobject;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;

/**
 * Loads the {@link ManagedObjectType} from the {@link ManagedObjectSource}.
 * 
 * @author Daniel
 */
public interface ManagedObjectLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link ManagedObjectSourceSpecification} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectSourceClass
	 *            {@link ManagedObjectSource} class.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return {@link PropertyList} of the {@link ManagedObjectSourceProperty}
	 *         instances of the {@link ManagedObjectSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> PropertyList loadSpecification(
			Class<MS> managedObjectSourceClass, CompilerIssues issues);

	/**
	 * Loads and returns the {@link ManagedObjectType} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectSourceClass
	 *            Class of the {@link ManagedObjectSource}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link ManagedObjectType}.
	 * @param classLoader
	 *            {@link ClassLoader} that the {@link ManagedObjectSource} may
	 *            use in obtaining necessary class path resources.
	 * @param issues
	 *            {@link CompilerIssues} to report issues in loading the
	 *            {@link ManagedObjectType}.
	 * @return {@link ManagedObjectType} or <code>null</code> if issues, which
	 *         are reported to the {@link CompilerIssues}.
	 */
	<D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> ManagedObjectType<D> loadManagedObject(
			Class<MS> managedObjectSourceClass, PropertyList propertyList,
			ClassLoader classLoader, CompilerIssues issues);

}