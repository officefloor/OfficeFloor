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
package net.officefloor.autowire.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.autowire.AutoWireGovernance;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.frame.api.governance.Governance;

/**
 * {@link AutoWireGovernance} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireGovernanceImpl extends AutoWirePropertiesImpl implements
		AutoWireGovernance {

	/**
	 * Name of the {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * {@link Class} name of the {@link GovernanceSource}.
	 */
	private final String governanceSourceClassName;

	/**
	 * {@link AutoWireSection} instances under this {@link Governance}.
	 */
	private final List<AutoWireSection> governedSections = new LinkedList<AutoWireSection>();

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceSourceClassName
	 *            {@link Class} name of the {@link GovernanceSource}.
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @param properties
	 *            {@link PropertyList}.
	 */
	public AutoWireGovernanceImpl(String governanceName,
			String governanceSourceClassName, OfficeFloorCompiler compiler,
			PropertyList properties) {
		super(compiler, properties);
		this.governanceName = governanceName;
		this.governanceSourceClassName = governanceSourceClassName;
	}

	/*
	 * ================== AutoWireGovernance ==========================
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

	@Override
	public String getGovernanceSourceClassName() {
		return this.governanceSourceClassName;
	}

	@Override
	public void governSection(AutoWireSection section) {
		this.governedSections.add(section);
	}

	@Override
	public AutoWireSection[] getGovernedSections() {
		return this.governedSections
				.toArray(new AutoWireSection[this.governedSections.size()]);
	}

}