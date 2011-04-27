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

package net.officefloor.frame.impl.execute.administrator;

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
import net.officefloor.frame.spi.administration.DutyKey;

/**
 * Implementation of an {@link AdministratorContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorContainerImpl<I extends Object, A extends Enum<A>, F extends Enum<F>>
		implements AdministratorContainer<I, A> {

	/**
	 * {@link DutyContext} that exposes only the required functionality.
	 */
	private final DutyContext<I, F> dutyContextToken = new DutyContextToken();

	/**
	 * {@link AdministratorMetaData}.
	 */
	private final AdministratorMetaData<I, A> metaData;

	/**
	 * {@link Administrator}.
	 */
	private Administrator<I, A> administrator;

	/**
	 * Initiate.
	 * 
	 * @param metaData
	 *            {@link AdministratorMetaData}.
	 */
	public AdministratorContainerImpl(AdministratorMetaData<I, A> metaData) {
		this.metaData = metaData;
	}

	/*
	 * ===================== AdministratorContainer =======================
	 */

	@Override
	public ExtensionInterfaceMetaData<I>[] getExtensionInterfaceMetaData(
			AdministratorContext context) {
		return this.metaData.getExtensionInterfaceMetaData();
	}

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

	@Override
	@SuppressWarnings("unchecked")
	public void doDuty(TaskDutyAssociation<A> taskDuty,
			List<I> extensionInterfaces, AdministratorContext context)
			throws Throwable {

		// Access Point: JobContainer -> WorkContainer
		// Locks: ThreadState

		// Lazy create the administrator
		if (this.administrator == null) {
			this.administrator = this.metaData.getAdministratorSource()
					.createAdministrator();
		}

		// Obtain the key identifying the duty
		DutyKey<A> key = taskDuty.getDutyKey();

		// Obtain the duty
		Duty<I, F> duty = (Duty<I, F>) this.administrator.getDuty(key);

		// Specify state
		this.adminContext = context;
		this.extensionInterfaces = extensionInterfaces;
		this.dutyMetaData = this.metaData.getDutyMetaData(key);

		// Execute the duty
		duty.doDuty(this.dutyContextToken);
	}

	/**
	 * <p>
	 * Token class given to the {@link Duty}.
	 * <p>
	 * As application code will be provided a {@link DutyContext} this exposes
	 * just the necessary functionality and prevents access to internals of the
	 * framework.
	 */
	private final class DutyContextToken implements DutyContext<I, F> {

		/*
		 * ==================== DutyContext ===================================
		 */

		@Override
		public List<I> getExtensionInterfaces() {
			return AdministratorContainerImpl.this.extensionInterfaces;
		}

		@Override
		public void doFlow(F key, Object parameter) {
			// Delegate with index of key
			this.doFlow(key.ordinal(), parameter);
		}

		@Override
		public void doFlow(int flowIndex, Object parameter) {
			// Obtain the flow meta-data
			FlowMetaData<?> flowMetaData = AdministratorContainerImpl.this.dutyMetaData
					.getFlow(flowIndex);

			// Do the flow
			AdministratorContainerImpl.this.adminContext.doFlow(flowMetaData,
					parameter);
		}
	}

}