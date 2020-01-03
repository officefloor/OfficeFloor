package net.officefloor.plugin.managedfunction.method;

import net.officefloor.compile.test.managedfunction.MockAsynchronousFlow;
import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil;
import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil.MethodResult;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests injecting {@link AsynchronousFlow}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousFlowInjectTest extends OfficeFrameTestCase {

	/**
	 * Ensure can inject {@link AsynchronousFlow}.
	 */
	public void testAsynchronousFlow() {
		AsynchronousFlowFunction instance = new AsynchronousFlowFunction();
		MethodResult result = MethodManagedFunctionBuilderUtil.runMethod(instance, "method", null, null);
		MockAsynchronousFlow[] asyncFlows = result.getAsynchronousFlows();
		assertEquals("Should have async flow", 1, asyncFlows.length);
		assertSame("Incorrect async flow", asyncFlows[0], instance.asyncFlow);
	}

	public static class AsynchronousFlowFunction {

		public AsynchronousFlow asyncFlow;

		public void method(AsynchronousFlow asyncFlow) {
			this.asyncFlow = asyncFlow;
		}
	}

}