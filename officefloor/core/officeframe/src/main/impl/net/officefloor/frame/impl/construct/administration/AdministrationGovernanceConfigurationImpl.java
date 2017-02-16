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
package net.officefloor.frame.impl.construct.administration;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.configuration.AdministrationGovernanceConfiguration;

/**
 * {@link AdministrationGovernanceConfiguration} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class AdministrationGovernanceConfigurationImpl<G extends Enum<G>>
		implements AdministrationGovernanceConfiguration<G> {

	/**
	 * Key of the {@link Governance}.
	 */
	private final G key;

	/**
	 * Index of the {@link Governance}.
	 */
	private final int index;

	/**
	 * Name of the {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * Instantiate.
	 * 
	 * @param key
	 *            Key of the {@link Governance}.
	 * @param index
	 *            Index of the {@link Governance}.
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 */
	public AdministrationGovernanceConfigurationImpl(G key, int index, String governanceName) {
		this.key = key;
		this.index = index;
		this.governanceName = governanceName;
	}

	/*
	 * ================= AdministrationGovernanceConfiguration =================
	 */

	@Override
	public G getKey() {
		return this.key;
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

}