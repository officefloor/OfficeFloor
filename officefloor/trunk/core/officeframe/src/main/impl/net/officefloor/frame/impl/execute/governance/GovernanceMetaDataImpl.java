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
package net.officefloor.frame.impl.execute.governance;

import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.source.GovernanceSource;

/**
 * {@link GovernanceMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceMetaDataImpl<I, F extends Enum<F>> implements
		GovernanceMetaData<I, F> {

	/**
	 * Name of the {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * {@link GovernanceSource}.
	 */
	private final GovernanceSource<I, F> governanceSource;

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceSource
	 *            {@link GovernanceSource}.
	 */
	public GovernanceMetaDataImpl(String governanceName,
			GovernanceSource<I, F> governanceSource) {
		this.governanceName = governanceName;
		this.governanceSource = governanceSource;
	}

	/*
	 * ================== GovernanceMetaData ==========================
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

	@Override
	public GovernanceContainer<I> createGovernanceContainer(Object processLock) {
		return new GovernanceContainerImpl<I, F>(this, processLock);
	}

	@Override
	public Governance<I, F> createGovernance() throws Throwable {
		return this.governanceSource.createGovernance();
	}

}