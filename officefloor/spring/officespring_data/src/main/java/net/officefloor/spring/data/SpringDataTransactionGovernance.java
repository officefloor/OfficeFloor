/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.spring.data;

import org.springframework.transaction.PlatformTransactionManager;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceContext;

/**
 * Spring Data transaction {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringDataTransactionGovernance implements Governance<PlatformTransactionManager, None> {

	@Override
	public void governManagedObject(PlatformTransactionManager managedObjectExtension, GovernanceContext<None> context)
			throws Throwable {
		// TODO implement
		// Governance<PlatformTransactionManager,None>.governManagedObject(...)
		throw new UnsupportedOperationException(
				"TODO implement Governance<PlatformTransactionManager,None>.governManagedObject(...)");
	}

	@Override
	public void enforceGovernance(GovernanceContext<None> context) throws Throwable {
		// TODO implement
		// Governance<PlatformTransactionManager,None>.enforceGovernance(...)
		throw new UnsupportedOperationException(
				"TODO implement Governance<PlatformTransactionManager,None>.enforceGovernance(...)");
	}

	@Override
	public void disregardGovernance(GovernanceContext<None> context) throws Throwable {
		// TODO implement
		// Governance<PlatformTransactionManager,None>.disregardGovernance(...)
		throw new UnsupportedOperationException(
				"TODO implement Governance<PlatformTransactionManager,None>.disregardGovernance(...)");
	}

}