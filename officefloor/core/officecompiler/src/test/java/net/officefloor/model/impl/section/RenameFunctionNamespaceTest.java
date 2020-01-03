package net.officefloor.model.impl.section;

import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.FunctionNamespaceModel;

/**
 * Tests renaming the {@link FunctionNamespaceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RenameFunctionNamespaceTest extends AbstractSectionChangesTestCase {

	/**
	 * Ensures handles {@link FunctionNamespaceModel} not being on the
	 * {@link SectionModel}.
	 */
	public void testRenameFunctionNamespaceNotOnSection() {
		FunctionNamespaceModel namespace = new FunctionNamespaceModel("NOT_IN_SECTION", null);
		Change<FunctionNamespaceModel> change = this.operations.renameFunctionNamespace(namespace, "NEW_NAME");
		this.assertChange(change, namespace, "Rename namespace NOT_IN_SECTION to NEW_NAME", false,
				"Function namespace NOT_IN_SECTION not in section");
	}

	/**
	 * Ensure can rename the {@link FunctionNamespaceModel}.
	 */
	public void testRenameFunctionNamespace() {
		FunctionNamespaceModel namespace = this.model.getFunctionNamespaces().get(0);
		Change<FunctionNamespaceModel> change = this.operations.renameFunctionNamespace(namespace, "NEW_NAME");
		this.assertChange(change, namespace, "Rename namespace OLD_NAME to NEW_NAME", true);
	}

	/**
	 * Ensures on renaming the {@link FunctionNamespaceModel} that order is
	 * maintained.
	 */
	public void testRenameFunctionNamespaceCausingFunctionNamespaceOrderChange() {
		this.useTestSetupModel();
		FunctionNamespaceModel namespace = this.model.getFunctionNamespaces().get(0);
		Change<FunctionNamespaceModel> change = this.operations.renameFunctionNamespace(namespace, "NAMESPACE_C");
		this.assertChange(change, namespace, "Rename namespace NAMESPACE_A to NAMESPACE_C", true);
	}
}