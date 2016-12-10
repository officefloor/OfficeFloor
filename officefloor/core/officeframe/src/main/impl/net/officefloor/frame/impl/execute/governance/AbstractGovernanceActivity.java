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

import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceControl;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.spi.governance.Governance;

/**
 * Abstract {@link GovernanceActivity}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractGovernanceActivity<I, F extends Enum<F>, C>
		implements GovernanceActivity<I, F> {

	/**
	 * {@link GovernanceMetaData}.
	 */
	private final GovernanceMetaData<I, F> metaData;

	/**
	 * {@link Governance} control.
	 */
	protected final C governanceControl;

	/**
	 * Initiate.
	 * 
	 * @param metaData
	 *            {@link GovernanceMetaData}.
	 * @param governanceControl
	 *            {@link GovernanceControl}.
	 */
	public AbstractGovernanceActivity(GovernanceMetaData<I, F> metaData,
			C governanceControl) {
		this.metaData = metaData;
		this.governanceControl = governanceControl;
	}

	/*
	 * ==================== GovernanceActivity ==============================
	 */

	@Override
	public GovernanceMetaData<I, F> getGovernanceMetaData() {
		return this.metaData;
	}

}