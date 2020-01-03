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