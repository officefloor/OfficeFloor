package net.officefloor.compile.impl.type;

import net.officefloor.compile.internal.structure.FunctionNamespaceNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;

/**
 * Tests loading the {@link FunctionNamespaceType} from the
 * {@link CompileContext}.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionTypeContextTest
		extends AbstractTestTypeContext<FunctionNamespaceNode, FunctionNamespaceType> {

	/**
	 * Instantiate.
	 */
	public ManagedFunctionTypeContextTest() {
		super(FunctionNamespaceNode.class, FunctionNamespaceType.class,
				(context, node) -> (FunctionNamespaceType) node.loadFunctionNamespaceType(),
				(context, node) -> (FunctionNamespaceType) context.getOrLoadFunctionNamespaceType(node));
	}

}