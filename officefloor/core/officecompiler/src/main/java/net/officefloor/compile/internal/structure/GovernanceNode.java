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
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.team.Team;

/**
 * {@link OfficeGovernance} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceNode extends LinkTeamNode, OfficeGovernance {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Governance";

	/**
	 * Initialises the {@link GovernanceNode}.
	 * 
	 * @param governanceSourceClassName
	 *            Class name of the {@link GovernanceSource}.
	 * @param governanceSource
	 *            Optional instantiated {@link GovernanceSource} to use. May be
	 *            <code>null</code>.
	 */
	void initialise(String governanceSourceClassName, GovernanceSource<?, ?> governanceSource);

	/**
	 * Loads the {@link GovernanceType} for this {@link GovernanceNode}.
	 * 
	 * @return {@link GovernanceType} for this {@link GovernanceNode} or
	 *         <code>null</code> if fails to load the {@link GovernanceType}.
	 */
	GovernanceType<?, ?> loadGovernanceType();

	/**
	 * Auto wires the {@link Team} for this {@link Governance}.
	 * 
	 * @param autoWirer
	 *            {@link AutoWirer}.
	 * @param compileContext
	 *            {@link CompileContext}.
	 */
	void autoWireTeam(AutoWirer<LinkTeamNode> autoWirer, CompileContext compileContext);

	/**
	 * Builds this {@link Governance} into the {@link OfficeBuilder}.
	 * 
	 * @param officeBuilder
	 *            {@link OfficeBuilder}.
	 * @param compileContext
	 *            {@link CompileContext}.
	 */
	void buildGovernance(OfficeBuilder officeBuilder, CompileContext compileContext);

}