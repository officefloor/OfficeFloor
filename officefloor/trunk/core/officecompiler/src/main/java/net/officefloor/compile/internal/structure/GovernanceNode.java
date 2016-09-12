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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.spi.governance.Governance;

/**
 * {@link OfficeGovernance} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceNode extends LinkTeamNode, OfficeGovernance {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Governance";

	/**
	 * Loads the {@link GovernanceType} for this {@link GovernanceNode}.
	 * 
	 * @return {@link GovernanceType} for this {@link GovernanceNode} or
	 *         <code>null</code> if fails to load the {@link GovernanceType}.
	 */
	GovernanceType<?, ?> loadGovernanceType();

	/**
	 * Builds this {@link Governance} into the {@link OfficeBuilder}.
	 * 
	 * @param officeBuilder
	 *            {@link OfficeBuilder}.
	 */
	void buildGovernance(OfficeBuilder officeBuilder);

}