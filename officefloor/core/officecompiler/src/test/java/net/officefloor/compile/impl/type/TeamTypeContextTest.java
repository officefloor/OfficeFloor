/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.type;

import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.team.TeamType;

/**
 * Tests loading the {@link TeamType} from the {@link CompileContext}.
 *
 * @author Daniel Sagenschneider
 */
public class TeamTypeContextTest extends AbstractTestTypeContext<TeamNode, TeamType> {

	/**
	 * Instantiate.
	 */
	public TeamTypeContextTest() {
		super(TeamNode.class, TeamType.class, (context, node) -> (TeamType) node.loadTeamType(),
				(context, node) -> (TeamType) context.getOrLoadTeamType(node));
	}

}