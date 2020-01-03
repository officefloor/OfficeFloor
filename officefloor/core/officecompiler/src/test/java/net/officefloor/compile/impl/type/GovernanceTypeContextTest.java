package net.officefloor.compile.impl.type;

import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.CompileContext;

/**
 * Tests loading the {@link GovernanceType} from the {@link CompileContext}.
 *
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("rawtypes")
public class GovernanceTypeContextTest extends AbstractTestTypeContext<GovernanceNode, GovernanceType> {

	/**
	 * Instantiate.
	 */
	public GovernanceTypeContextTest() {
		super(GovernanceNode.class, GovernanceType.class, (context, node) -> (GovernanceType) node.loadGovernanceType(),
				(context, node) -> (GovernanceType) context.getOrLoadGovernanceType(node));
	}

}