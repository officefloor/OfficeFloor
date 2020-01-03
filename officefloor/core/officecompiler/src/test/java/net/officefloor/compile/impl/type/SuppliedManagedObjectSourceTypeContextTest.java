package net.officefloor.compile.impl.type;

import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectSourceNode;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;

/**
 * Tests loading the {@link SuppliedManagedObjectSourceType} from the
 * {@link CompileContext}.
 *
 * @author Daniel Sagenschneider
 */
public class SuppliedManagedObjectSourceTypeContextTest
		extends AbstractTestTypeContext<SuppliedManagedObjectSourceNode, SuppliedManagedObjectSourceType> {

	/**
	 * Instantiate.
	 */
	public SuppliedManagedObjectSourceTypeContextTest() {
		super(SuppliedManagedObjectSourceNode.class, SuppliedManagedObjectSourceType.class,
				(context, node) -> node.loadSuppliedManagedObjectSourceType(context),
				(context, node) -> context.getOrLoadSuppliedManagedObjectSourceType(node));
	}

}