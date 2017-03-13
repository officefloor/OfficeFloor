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
package net.officefloor.compile.spi.administration.source;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Meta-data of the {@link AdministrationSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationSourceMetaData<E, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * Obtains the {@link Class} that the {@link ManagedObject} must provide as
	 * an extension interface to be administered.
	 * 
	 * @return Extension interface for the {@link ManagedObject}.
	 */
	Class<E> getExtensionInterface();

	/**
	 * Obtains the {@link AdministrationFactory} to create the
	 * {@link Administration} for this {@link AdministrationMetaData}.
	 * 
	 * @return {@link AdministrationFactory}
	 */
	AdministrationFactory<E, F, G> getAdministrationFactory();

	/**
	 * Obtains the list of {@link AdministrationFlowMetaData} instances should
	 * this {@link Administration} require instigating a {@link Flow}.
	 * 
	 * @return Meta-data of {@link Flow} instances instigated by this
	 *         {@link Administration}.
	 */
	AdministrationFlowMetaData<F>[] getFlowMetaData();

}