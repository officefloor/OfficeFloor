package net.officefloor.plugin.managedfunction.method;

import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure can inject {@link ManagedFunctionContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionContextInjectTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to inject {@link ManagedFunctionContext}.
	 */
	public void testManagedFunctionContext() {
		Closure<ManagedFunctionContext<?, ?>> managedFunctionContext = new Closure<>();
		ManagedFunctionContextFunction instance = new ManagedFunctionContextFunction();
		MethodManagedFunctionBuilderUtil.runMethod(instance, "method", null, (context) -> {
			managedFunctionContext.value = (ManagedFunctionContext<?, ?>) context;
		});
		assertSame("Should inject the managed function context", managedFunctionContext.value, instance.context);
	}

	public static class ManagedFunctionContextFunction {

		public ManagedFunctionContext<?, ?> context;

		public void method(ManagedFunctionContext<?, ?> context) {
			this.context = context;
		}
	}

}