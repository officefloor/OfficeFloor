package net.officefloor.compile.impl.type;

import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.managedobject.ManagedObjectType;

/**
 * Tests loading the {@link ManagedObjectType} from the {@link CompileContext}.
 *
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("rawtypes")
public class ManagedObjectTypeContextTest extends AbstractTestTypeContext<ManagedObjectSourceNode, ManagedObjectType> {

	/**
	 * Instantiate.
	 */
	public ManagedObjectTypeContextTest() {
		super(ManagedObjectSourceNode.class, ManagedObjectType.class,
				(context, node) -> (ManagedObjectType) node.loadManagedObjectType(context),
				(context, node) -> (ManagedObjectType) context.getOrLoadManagedObjectType(node));
	}

}