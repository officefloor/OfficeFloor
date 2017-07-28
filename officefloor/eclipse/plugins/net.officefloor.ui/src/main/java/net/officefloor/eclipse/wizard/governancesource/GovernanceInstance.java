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
package net.officefloor.eclipse.wizard.governancesource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.frame.api.governance.Governance;

/**
 * Instance of a {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceInstance {

	/**
	 * Name of this {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * {@link GovernanceSource} class name.
	 */
	private final String governanceSourceClassName;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link GovernanceType}.
	 */
	private final GovernanceType<?, ?> governanceType;

	/**
	 * Initiate for public use.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceSourceClassName
	 *            {@link GovernanceSource} class name.
	 */
	public GovernanceInstance(String governanceName, String governanceSourceClassName) {
		this.governanceName = governanceName;
		this.governanceSourceClassName = governanceSourceClassName;
		this.propertyList = OfficeFloorCompiler.newPropertyList();
		this.governanceType = null;
	}

	/**
	 * Initiate from {@link GovernanceSourceInstance}.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceSourceClassName
	 *            {@link GovernanceSource} class name.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param governanceType
	 *            {@link GovernanceType}.
	 */
	GovernanceInstance(String governanceName, String governanceSourceClassName, PropertyList propertyList,
			GovernanceType<?, ?> governanceType) {
		this.governanceName = governanceName;
		this.governanceSourceClassName = governanceSourceClassName;
		this.propertyList = propertyList;
		this.governanceType = governanceType;
	}

	/**
	 * Obtains the name of the {@link Governance}.
	 * 
	 * @return Name of the {@link Governance}.
	 */
	public String getGovernanceName() {
		return this.governanceName;
	}

	/**
	 * Obtains the {@link GovernanceSource} class name.
	 * 
	 * @return {@link GovernanceSource} class name.
	 */
	public String getGovernanceSourceClassName() {
		return this.governanceSourceClassName;
	}

	/**
	 * Obtains the {@link PropertyList}.
	 * 
	 * @return {@link PropertyList}.
	 */
	public PropertyList getPropertyList() {
		return this.propertyList;
	}

	/**
	 * Obtains the {@link GovernanceType}.
	 * 
	 * @return {@link GovernanceType} if obtained from
	 *         {@link GovernanceSourceInstance} or <code>null</code> if
	 *         initiated by <code>public</code> constructor.
	 */
	public GovernanceType<?, ?> getGovernanceType() {
		return this.governanceType;
	}

}