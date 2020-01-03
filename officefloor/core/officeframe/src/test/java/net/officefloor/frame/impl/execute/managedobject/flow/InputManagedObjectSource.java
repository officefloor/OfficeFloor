package net.officefloor.frame.impl.execute.managedobject.flow;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Invokes flows to input data to Office.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class InputManagedObjectSource extends AbstractManagedObjectSource<None, InputManagedObjectSource.Flows> {

	/**
	 * Keys of {@link Flow} instances instigated.
	 */
	public enum Flows {
		INPUT
	}

	/**
	 * Instance.
	 */
	private static InputManagedObjectSource INSTANCE;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<Flows> executeContext;

	/**
	 * Inputs a parameter into the Office.
	 * 
	 * @param parameter
	 *            Parameter to input into the Office.
	 * @param managedObject
	 *            {@link ManagedObject}.
	 * @param delay
	 *            Delay to invoke process.
	 */
	public static void input(Object parameter, ManagedObject managedObject, long delay) {
		// Input the parameter
		INSTANCE.executeContext.invokeProcess(Flows.INPUT, parameter, managedObject, delay, null);
	}

	/**
	 * Initialise and make available to invoke a process.
	 */
	public InputManagedObjectSource() {
		INSTANCE = this;
	}

	/*
	 * ==================== AbstractManagedObjectSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		INSTANCE = this;
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, Flows> context) throws Exception {
		context.setObjectClass(this.getClass());
		context.addFlow(Flows.INPUT, Object.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {
		this.executeContext = context;
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return null;
	}

}