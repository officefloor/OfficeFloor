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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.spi.administration.source.AdministratorSource;

/**
 * Meta-data of the
 * {@link net.officefloor.frame.spi.administration.Administrator}.
 * 
 * @author Daniel
 */
public interface AdministratorMetaData<I extends Object, A extends Enum<A>> {

	/**
	 * Index indicating the
	 * {@link net.officefloor.frame.spi.administration.Administrator} will not
	 * be sourced from the {@link ProcessState}.
	 */
	static final int NON_PROCESS_INDEX = -1;

	/**
	 * <p>
	 * Obtains the index of the
	 * {@link net.officefloor.frame.spi.administration.Administrator} within the
	 * {@link ProcessState}.
	 * <p>
	 * Note that if this does not provide a value of {@link #NON_PROCESS_INDEX}
	 * then the {@link net.officefloor.frame.spi.administration.Administrator}
	 * will be sourced only for the
	 * {@link net.officefloor.frame.api.execute.Work}.
	 * 
	 * @return Index of the
	 *         {@link net.officefloor.frame.spi.administration.Administrator}
	 *         within the {@link ProcessState} or {@link #NON_PROCESS_INDEX}.
	 */
	int getProcessStateAdministratorIndex();

	/**
	 * Obtains the {@link AdministratorSource}.
	 * 
	 * @return {@link AdministratorSource}.
	 */
	AdministratorSource<I, A> getAdministratorSource();

	/**
	 * Obtains the {@link ExtensionInterfaceMetaData} over the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} instances
	 * to be administered by this
	 * {@link net.officefloor.frame.spi.administration.Administrator}.
	 * 
	 * @return {@link ExtensionInterfaceMetaData} over the
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *         instances to be administered by this
	 *         {@link net.officefloor.frame.spi.administration.Administrator}.
	 */
	ExtensionInterfaceMetaData<I>[] getExtensionInterfaceMetaData();

	/**
	 * Obtains the {@link DutyMetaData} for the input key.
	 * 
	 * @param key
	 *            Key specifying the
	 *            {@link net.officefloor.frame.spi.administration.Duty}.
	 * @return {@link DutyMetaData} for the specified
	 *         {@link net.officefloor.frame.spi.administration.Duty}.
	 */
	DutyMetaData getDutyMetaData(A key);

}
