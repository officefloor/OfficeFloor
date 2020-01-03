package net.officefloor.compile.impl.type;

import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.internal.structure.AdministrationNode;
import net.officefloor.compile.internal.structure.CompileContext;

/**
 * Tests loading the {@link AdministrationType} from the {@link CompileContext}.
 *
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("rawtypes")
public class AdministrationTypeContextTest extends AbstractTestTypeContext<AdministrationNode, AdministrationType> {

	/**
	 * Instantiate.
	 */
	public AdministrationTypeContextTest() {
		super(AdministrationNode.class, AdministrationType.class,
				(context, node) -> (AdministrationType) node.loadAdministrationType(),
				(context, node) -> (AdministrationType) context.getOrLoadAdministrationType(node));
	}

}