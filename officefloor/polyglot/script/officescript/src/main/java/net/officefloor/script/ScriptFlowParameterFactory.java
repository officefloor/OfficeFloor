package net.officefloor.script;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;

/**
 * {@link ScriptFlow} {@link MethodParameterFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ScriptFlowParameterFactory implements MethodParameterFactory {

	/**
	 * Index of the {@link Flow}.
	 */
	private final int flowIndex;

	/**
	 * Instantiate.
	 * 
	 * @param flowIndex Index of the {@link Flow}.
	 */
	public ScriptFlowParameterFactory(int flowIndex) {
		this.flowIndex = flowIndex;
	}

	/*
	 * ====================== ManagedFunctionParameterFactory ======================
	 */

	@Override
	public Object createParameter(ManagedFunctionContext<?, ?> context) throws Exception {
		return new ScriptFlowImpl(context);
	}

	/**
	 * {@link ScriptFlow} implementation.
	 */
	private class ScriptFlowImpl implements ScriptFlow {

		/**
		 * {@link ManagedFunctionContext}.
		 */
		private final ManagedFunctionContext<?, ?> context;

		/**
		 * Instantiate.
		 * 
		 * @param context {@link ManagedFunctionContext}.
		 */
		private ScriptFlowImpl(ManagedFunctionContext<?, ?> context) {
			this.context = context;
		}

		/*
		 * ====================== ScriptFlow ================================
		 */

		@Override
		public void doFlow(Object parameter, FlowCallback callback) {
			this.context.doFlow(ScriptFlowParameterFactory.this.flowIndex, parameter, callback);
		}
	}

}