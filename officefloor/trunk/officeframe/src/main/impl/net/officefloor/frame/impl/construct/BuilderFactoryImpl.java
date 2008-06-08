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
package net.officefloor.frame.impl.construct;

import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.BuilderFactory;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Implementation of the {@link BuilderFactory}.
 * 
 * @author Daniel
 */
public class BuilderFactoryImpl implements BuilderFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.BuilderFactory#createOfficeFloorBuilder()
	 */
	public OfficeFloorBuilder createOfficeFloorBuilder() {
		return new OfficeFloorBuilderImpl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.BuilderFactory#createOfficeBuilder()
	 */
	public OfficeBuilder createOfficeBuilder() {
		return new OfficeBuilderImpl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.BuilderFactory#createWorkBuilder(java.lang.Class)
	 */
	public <W extends Work> WorkBuilder<W> createWorkBuilder(Class<W> typeOfWork) {
		return new WorkBuilderImpl<W>(typeOfWork);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.BuilderFactory#createManagedObjectBuilder(java.lang.Class)
	 */
	@Override
	public <D extends Enum<D>, H extends Enum<H>, MS extends ManagedObjectSource<D, H>> ManagedObjectBuilder<H> createManagedObjectBuilder(
			Class<MS> managedObjectSourceClass) {
		return new ManagedObjectBuilderImpl<D, H, MS>(managedObjectSourceClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.BuilderFactory#createAdministratorBuilder(java.lang.Class)
	 */
	@Override
	public <I, A extends Enum<A>, AS extends AdministratorSource<I, A>> AdministratorBuilder<A> createAdministratorBuilder(
			Class<AS> administratorSourceClass) {
		return new AdministratorBuilderImpl<I, A, AS>(administratorSourceClass);
	}

}
