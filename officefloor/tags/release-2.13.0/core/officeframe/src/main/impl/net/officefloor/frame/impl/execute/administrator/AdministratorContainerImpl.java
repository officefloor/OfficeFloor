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
package net.officefloor.frame.impl.execute.administrator;

import java.util.List;

import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.ContainerContext;
import net.officefloor.frame.internal.structure.DutyMetaData;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.DutyContext;
import net.officefloor.frame.spi.administration.DutyKey;
import net.officefloor.frame.spi.administration.GovernanceManager;
import net.officefloor.frame.spi.governance.Governance;

/**
 * Implementation of an {@link AdministratorContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorContainerImpl<I extends Object, A extends Enum<A>, F extends Enum<F>, G extends Enum<G>>
		implements AdministratorContainer<I, A> {

	/**
	 * {@link AdministratorMetaData}.MetaData} for disregarding the
	 * {@link Governance}.
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

	@Override
	@SuppressWarnings("unchecked")
	public void doDuty(TaskDutyAssociation<A> taskDuty,
			List<I> extensionInterfaces, AdministratorContext context,
			ContainerContext containerContext) throws Throwable {

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
		Duty<I, F, G> duty = (Duty<I, F, G>) this.administrator.getDuty(key);

		// Obtain the duty meta-data
		DutyMetaData dutyMetaData = this.metaData.getDutyMetaData(key);

		// Execute the duty
		duty.doDuty(new DutyContextToken(context, extensionInterfaces,
				dutyMetaData, containerContext));
	}

	/**
	 * <p>
	 * Token class given to the {@link Duty}.
	 * <p>
	 * As application code will be provided a {@link DutyContext} this exposes
	 * just the necessary functionality and prevents access to internals of the
	 * framework.
	 */
	private final class DutyContextToken implements DutyContext<I, F, G> {

		/**
		 * {@link AdministratorContext}.
		 */
		private final AdministratorContext adminContext;

		/**
		 * Extension interfaces.
		 */
		private final List<I> extensionInterfaces;

		/**
		 * {@link DutyMetaData}.
		 */
		private final DutyMetaData dutyMetaData;

		/**
		 * {@link ContainerContext}.
		 */
		private final ContainerContext containerContext;

		/**
		 * Initiate.
		 * 
		 * @param adminContext
		 *            {@link AdministratorContext}.
		 * @param extensionInterfaces
		 *            Extension interfaces.
		 * @param dutyMetaData
		 *            {@link DutyMetaData}.
		 * @param containerContext
		 *            {@link ContainerContext}.
		 */
		public DutyContextToken(AdministratorContext adminContext,
				List<I> extensionInterfaces, DutyMetaData dutyMetaData,
				ContainerContext containerContext) {
			this.adminContext = adminContext;
			this.extensionInterfaces = extensionInterfaces;
			this.dutyMetaData = dutyMetaData;
			this.containerContext = containerContext;
		}

		/*
		 * ==================== DutyContext ===================================
		 */

		@Override
		public List<I> getExtensionInterfaces() {
			return this.extensionInterfaces;
		}

		@Override
		public void doFlow(F key, Object parameter) {
			// Delegate with index of key
			this.doFlow(key.ordinal(), parameter);
		}

		@Override
		public void doFlow(int flowIndex, Object parameter) {
			// Obtain the flow meta-data
			FlowMetaData<?> flowMetaData = this.dutyMetaData.getFlow(flowIndex);

			// Do the flow
			this.adminContext.doFlow(flowMetaData, parameter);
		}

		@Override
		public GovernanceManager getGovernance(G key) {
			return this.getGovernance(key.ordinal());
		}

		@Override
		public GovernanceManager getGovernance(int governanceIndex) {

			// Obtain the process index for the governance
			int processIndex = this.dutyMetaData
					.translateGovernanceIndexToThreadIndex(governanceIndex);

			// Create Governance Manager to wrap Governance Container
			GovernanceManager manager = new GovernanceManagerImpl(
					this.adminContext, processIndex, this.containerContext);

			// Return the governance manager
			return manager;
		}
	}

}