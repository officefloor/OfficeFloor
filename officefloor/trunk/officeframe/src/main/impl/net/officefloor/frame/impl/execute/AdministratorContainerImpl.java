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
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.DutyMetaData;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.DutyContext;

/**
 * Implementation of an
 * {@link net.officefloor.frame.internal.structure.AdministratorContainer}.
 * 
 * @author Daniel
 */
public class AdministratorContainerImpl<I extends Object, A extends Enum<A>, F extends Enum<F>>
		implements AdministratorContainer<I, A>, DutyContext<I, F> {

	/**
	 * {@link AdministratorMetaData}.
	 */
	protected final AdministratorMetaData<I, A> metaData;

	/**
	 * {@link Administrator}.
	 */
	protected Administrator<I, A> administrator;

	/**
	 * Initiate.
	 * 
	 * @param metaData
	 *            {@link AdministratorMetaData}.
	 */
	public AdministratorContainerImpl(AdministratorMetaData<I, A> metaData) {
		// Store state
		this.metaData = metaData;
	}

	/*
	 * ====================================================================
	 * AdministratorContainer
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AdministratorContainer#getExtensionInterfaceMetaData(net.officefloor.frame.internal.structure.AdministratorContext)
	 */
	public ExtensionInterfaceMetaData<I>[] getExtensionInterfaceMetaData(
			AdministratorContext context) {
		return this.metaData.getExtensionInterfaceMetaData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AdministratorContainer#doDuty(net.officefloor.frame.internal.structure.DutyMetaData)
	 */
	@SuppressWarnings("unchecked")
	public void doDuty(TaskDutyAssociation<A> taskDuty,
			List<I> extensionInterfaces, AdministratorContext context)
			throws Exception {

		// Access Point: TaskContainer
		// Locks: ThreadState -> ProcessState

		// Lazy create the administrator
		if (this.administrator == null) {
			this.administrator = this.metaData.getAdministratorSource()
					.createAdministrator();
		}

		// Obtain the key identifying the duty
		A key = taskDuty.getDutyKey();

		// Obtain the duty
		Duty<I, F> duty = (Duty<I, F>) this.administrator.getDuty(key);

		// Specify state
		this.adminContext = context;
		this.extensionInterfaces = extensionInterfaces;
		this.dutyMetaData = this.metaData.getDutyMetaData(key);

		// Execute the duty
		duty.doDuty(this);
	}

	/*
	 * ====================================================================
	 * DutyContext
	 * ====================================================================
	 */

	/**
	 * {@link AdministratorContext}.
	 */
	private AdministratorContext adminContext;

	/**
	 * Extension interfaces.
	 */
	private List<I> extensionInterfaces;

	/**
	 * {@link DutyMetaData}.
	 */
	private DutyMetaData dutyMetaData;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.DutyContext#getExtensionInterfaces()
	 */
	public List<I> getExtensionInterfaces() {
		return this.extensionInterfaces;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.DutyContext#doFlow(F,
	 *      java.lang.Object)
	 */
	public void doFlow(F key, Object parameter) {
		// Delegate with index of key
		this.doFlow(key.ordinal(), parameter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.DutyContext#doFlow(int,
	 *      java.lang.Object)
	 */
	public void doFlow(int flowIndex, Object parameter) {
		// Obtain the flow meta-data
		FlowMetaData<?> flowMetaData = this.dutyMetaData.getFlow(flowIndex);

		// Do the flow
		this.adminContext.doFlow(flowMetaData, parameter);
	}

}
