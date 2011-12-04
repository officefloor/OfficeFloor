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
package net.officefloor.plugin.autowire;

import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.frame.spi.governance.Governance;

/**
 * {@link Governance} for configuring auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireGovernance extends AutoWireProperties {

	/**
	 * Obtains the {@link Governance} name.
	 * 
	 * @return Name of the {@link Governance}.
	 */
	String getGovernanceName();

	/**
	 * Obtains the {@link GovernanceSource} class.
	 * 
	 * @return {@link GovernanceSource} class.
	 */
	Class<?> getGovernanceSourceClass();

	/**
	 * Provides {@link Governance} over the {@link AutoWireSection}.
	 * 
	 * @param section
	 *            {@link AutoWireSection} to have {@link Governance} provided
	 *            from this {@link AutoWireGovernance}.
	 */
	void governSection(AutoWireSection section);

	/**
	 * Obtains the {@link AutoWireSection} instances under this
	 * {@link Governance}.
	 * 
	 * @return {@link AutoWireSection} instances under this {@link Governance}.
	 */
	AutoWireSection[] getGovernedSections();

}