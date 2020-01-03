package net.officefloor.compile.impl.type;

import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.supplier.SupplierType;

/**
 * Tests loading the {@link SupplierType} from the {@link CompileContext}.
 *
 * @author Daniel Sagenschneider
 */
public class SupplierTypeContextTest extends AbstractTestTypeContext<SupplierNode, SupplierType> {

	/**
	 * Instantiate.
	 */
	public SupplierTypeContextTest() {
		super(SupplierNode.class, SupplierType.class, (context, node) -> node.loadSupplierType(),
				(context, node) -> context.getOrLoadSupplierType(node));
	}

}