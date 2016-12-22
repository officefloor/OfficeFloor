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
package net.officefloor.frame.impl.execute.governance;

import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.work.WorkMetaDataImpl;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.spi.governance.Governance;

/**
 * {@link Governance} {@link Work}.
 * 
 * @author Daniel Sagenschneider
 */
@Deprecated // with work being deprecated
public class GovernanceWork extends WorkMetaDataImpl<Work> {

	/**
	 * Initiate.
	 */
	@SuppressWarnings("unchecked")
	public GovernanceWork() {
		super("GOVERNANCE", new GovernanceWorkFactory(), new ManagedObjectMetaData<?>[0],
				new AdministratorMetaData<?, ?>[0], null, new ManagedFunctionMetaData[0]);
	}

	/**
	 * {@link WorkFactory} for the {@link GovernanceWork}.
	 */
	private static class GovernanceWorkFactory implements WorkFactory<Work>, Work {

		/*
		 * ====================== WorkFactory ===========================
		 */

		@Override
		public Work createWork() {
			return this;
		}
	}

}