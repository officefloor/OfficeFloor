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

/**
 * Implementation of the {@link net.officefloor.frame.api.build.BuilderFactory}.
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
	 * @see net.officefloor.frame.api.construct.MetaDataFactory#createManagedObjectBuilder(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public ManagedObjectBuilder<?> createManagedObjectBuilder() {
		return new ManagedObjectBuilderImpl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.BuilderFactory#createTaskAdministratorBuilder()
	 */
	@SuppressWarnings("unchecked")
	public AdministratorBuilder<?> createAdministratorBuilder() {
		return new AdministratorBuilderImpl();
	}

}
