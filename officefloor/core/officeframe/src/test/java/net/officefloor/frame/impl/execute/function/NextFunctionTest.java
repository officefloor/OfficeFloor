package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure invokes the next {@link ManagedFunction}.
 *
 * @author Daniel Sagenschneider
 */
public class NextFunctionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can invoke next {@link ManagedFunction} without a parmaeter.
	 */
	public void testInvokeNextFunction() throws Exception {
		this.doInvokeNextFunctionTest(null);
	}

	/**
	 * Ensure can invoke next {@link ManagedFunction} with a parameter.
	 */
	public void testInvokeNextFunctionWithParameter() throws Exception {
		this.doInvokeNextFunctionTest("TEST");
	}

	/**
	 * Undertakes the next {@link ManagedFunction} test.
	 * 
	 * @param parameter
	 *            Parameter.
	 */
	public void doInvokeNextFunctionTest(String parameter) throws Exception {
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildParameter();
		task.setNextFunction("next");
		this.constructFunction(work, "next").buildParameter();
		this.invokeFunction("task", parameter);
		assertTrue("Next function should be invoked", work.isNextInvoked);
		assertEquals("Incorrect next parameter", parameter, work.nextParameter);
	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {

		public boolean isNextInvoked = false;

		public String nextParameter = null;

		public String task(String parameter) {
			return parameter;
		}

		public void next(String parameter) {
			this.isNextInvoked = true;
			this.nextParameter = parameter;
		}
	}

}
