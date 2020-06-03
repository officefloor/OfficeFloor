package net.officefloor.plugin.clazz.dependency.impl;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.flow.ClassFlowInterfaceFactory;

/**
 * {@link ClassDependencyFactory} for {@link FlowInterface},
 * 
 * @author Daniel Sagenschneider
 */
public class FlowClassDependencyFactory implements ClassDependencyFactory {

	/**
	 * {@link ClassFlowInterfaceFactory}.
	 */
	private final ClassFlowInterfaceFactory factory;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<Indexed> executeContext;

	/**
	 * Instantiate.
	 * 
	 * @param factory {@link ClassFlowInterfaceFactory}.
	 */
	public FlowClassDependencyFactory(ClassFlowInterfaceFactory factory) {
		this.factory = factory;
	}

	/*
	 * ================== ClassDependencyFactory =======================
	 */

	@Override
	public void loadManagedObjectExecuteContext(ManagedObjectExecuteContext<Indexed> executeContext) {
		this.executeContext = executeContext;
	}

	@Override
	public Object createDependency(ManagedObject managedObject, ManagedObjectContext context,
			ObjectRegistry<Indexed> registry) throws Exception {
		return this.factory.createFlows((flowIndex, parameter, callback) -> this.executeContext.invokeProcess(flowIndex,
				parameter, managedObject, 0, callback));
	}

	@Override
	public Object createDependency(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {
		return this.factory
				.createFlows((flowIndex, parameter, callback) -> context.doFlow(flowIndex, parameter, callback));
	}

}