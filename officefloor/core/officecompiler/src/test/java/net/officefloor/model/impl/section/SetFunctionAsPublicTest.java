package net.officefloor.model.impl.section;

import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.FunctionModel;

/**
 * Tests setting the {@link FunctionModel} as public.
 * 
 * @author Daniel Sagenschneider
 */
public class SetFunctionAsPublicTest extends AbstractSectionChangesTestCase {

	/**
	 * Public {@link FunctionModel}.
	 */
	private FunctionModel publicFunction;

	/**
	 * Private {@link FunctionModel}.
	 */
	private FunctionModel privateFunction;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the public and private functions
		this.publicFunction = this.model.getFunctions().get(0);
		this.privateFunction = this.model.getFunctions().get(1);
	}

	/**
	 * Ensure no change if the {@link FunctionModel} not on the
	 * {@link SectionModel}.
	 */
	public void testFunctionNotInSection() {
		FunctionModel function = new FunctionModel("FUNCTION", false, "NAMESPACE", "MANAGED_FUNCTION", null);
		Change<FunctionModel> change = this.operations.setFunctionAsPublic(true, function);
		this.assertChange(change, function, "Set function FUNCTION public", false, "Function FUNCTION not in section");
	}

	/**
	 * Ensures can set a {@link FunctionModel} to be public.
	 */
	public void testFunctionPublic() {
		Change<FunctionModel> change = this.operations.setFunctionAsPublic(true, this.privateFunction);
		this.assertChange(change, this.privateFunction, "Set function PRIVATE public", true);
	}

	/**
	 * Ensures can set a {@link FunctionModel} to be private.
	 */
	public void testSetFunctionPrivate() {
		Change<FunctionModel> change = this.operations.setFunctionAsPublic(false, this.publicFunction);
		this.assertChange(change, this.publicFunction, "Set function PUBLIC private", true);
	}

}