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
package net.officefloor.frame.impl.execute;

import java.util.List;

import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;

/**
 * <p>
 * Proxy for the
 * {@link net.officefloor.frame.internal.structure.AdministratorContainer}.
 * <p>
 * This is used to contain
 * {@link net.officefloor.frame.spi.administration.Administrator} instances
 * bound to the process.
 * 
 * @author Daniel
 */
public class AdministratorContainerProxy<I extends Object, A extends Enum<A>>
		implements AdministratorContainer<I, A> {

	/**
	 * Index of the
	 * {@link net.officefloor.frame.spi.administration.Administrator} for this
	 * {@link AdministratorContainer} within the {@link ProcessState}.
	 */
	protected final int index;

	/**
	 * Initiate.
	 * 
	 * @param index
	 *            Index of the
	 *            {@link net.officefloor.frame.spi.administration.Administrator}
	 *            for this {@link AdministratorContainer} within the
	 *            {@link ProcessState}.
	 */
	public AdministratorContainerProxy(int index) {
		this.index = index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AdministratorContainer#getExtensionInterfaceMetaData(net.officefloor.frame.internal.structure.AdministratorContext)
	 */
	@SuppressWarnings("unchecked")
	public ExtensionInterfaceMetaData<I>[] getExtensionInterfaceMetaData(
			AdministratorContext context) {
		return context.getThreadState().getProcessState()
				.getAdministratorContainer(this.index)
				.getExtensionInterfaceMetaData(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AdministratorContainer#doDuty(net.officefloor.frame.internal.structure.TaskDutyAssociation,
	 *      I[], net.officefloor.frame.internal.structure.AdministratorContext)
	 */
	@SuppressWarnings("unchecked")
	public void doDuty(TaskDutyAssociation taskDutyAssociation,
			List<I> extensionInterfaces, AdministratorContext context)
			throws Exception {
		context.getThreadState().getProcessState().getAdministratorContainer(
				this.index).doDuty(taskDutyAssociation, extensionInterfaces,
				context);
	}

}
