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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Creates meta-data items.
 * 
 * @author Daniel
 */
public interface BuilderFactory {

	/**
	 * Creates a new {@link OfficeFloorBuilder}.
	 * 
	 * @return New {@link OfficeFloorBuilder}.
	 */
	OfficeFloorBuilder createOfficeFloorBuilder();

	/**
	 * Creates a new {@link OfficeBuilder}.
	 * 
	 * @return New {@link OfficeBuilder}.
	 */
	OfficeBuilder createOfficeBuilder();

	/**
	 * Creates the {@link WorkBuilder}.
	 * 
	 * @param W
	 *            Specific {@link Work}.
	 * @param typeOfWork
	 *            {@link Class} of the {@link Work}.
	 * @return Specific {@link WorkBuilder}.
	 */
	<W extends Work> WorkBuilder<W> createWorkBuilder(Class<W> typeOfWork);

	/**
	 * Creates the {@link ManagedObjectBuilder}.
	 * 
	 * @param managedObjectSourceClass
	 *            {@link Class} of the {@link ManagedObjectSource}.
	 * @return {@link ManagedObjectBuilder}.
	 */
	<D extends Enum<D>, H extends Enum<H>, MS extends ManagedObjectSource<D, H>> ManagedObjectBuilder<H> createManagedObjectBuilder(
			Class<MS> managedObjectSourceClass);

	/**
	 * Creates the {@link AdministratorBuilder}.
	 * 
	 * @return {@link AdministratorBuilder}.
	 */
	<I, A extends Enum<A>, AS extends AdministratorSource<I, A>> AdministratorBuilder<A> createAdministratorBuilder(
			Class<AS> administratorSourceClass);

}
