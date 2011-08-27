/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.autowire;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * Provides a singleton object.
 * 
 * @author Daniel Sagenschneider
 */
public class SingletonManagedObjectSource extends
		AbstractManagedObjectSource<None, None> implements ManagedObject {

	/**
	 * Singleton.
	 */
	private final Object object;

	/**
	 * Initiate.
	 * 
	 * @param object
	 *            Singleton object.
	 */
	public SingletonManagedObjectSource(Object object) {
		this.object = object;
	}

	/*
	 * ========================== ManagedObjectSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		context.setObjectClass(this.object.getClass());
	}

	@Override
	protected ManagedObject getManagedObject() {
		return this;
	}

	/*
	 * ============================ ManagedObject ============================
	 */

	@Override
	public Object getObject() {
		return this.object;
	}

}