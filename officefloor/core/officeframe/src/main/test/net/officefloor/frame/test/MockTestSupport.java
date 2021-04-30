/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.test;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IMocksControl;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.officefloor.frame.test.match.ArgumentsMatcher;

/**
 * Mock test support.
 * 
 * @author Daniel Sagenschneider
 */
public class MockTestSupport implements TestSupport {

	/**
	 * {@link EasyMockSupport}.
	 */
	private final EasyMockSupport easyMockSupport = new EasyMockSupport();

	/**
	 * Map of object to its {@link IMocksControl}.
	 */
	private final Map<Object, IMocksControl> mockRegistry = new HashMap<>();

	/*
	 * ======================== TestSupport ==========================
	 */

	private LogTestSupport logTestSupport;

	/**
	 * Initiate.
	 * 
	 * @param logTestSupport {@link LogTestSupport}.
	 */
	public MockTestSupport(LogTestSupport logTestSupport) {
		this.logTestSupport = logTestSupport;
	}

	/**
	 * Default instantiation.
	 */
	public MockTestSupport() {
		// Initialised with test support
	}

	@Override
	public void init(ExtensionContext context) throws Exception {
		this.logTestSupport = TestSupportExtension.getTestSupport(LogTestSupport.class, context);
	}

	/**
	 * Creates a mock object registering the mock object with registry for
	 * management.
	 * 
	 * @param <M>         Interface type.
	 * @param classToMock {@link Class} to be mocked.
	 * @return Mock object.
	 */
	public <M> M createMock(Class<M> classToMock) {
		return this.createMock(classToMock, false);
	}

	/**
	 * Creates a thread safe mock object.
	 * 
	 * @param <M>             Interface type.
	 * @param interfaceToMock {@link Class} to mock.
	 * @return Mock object.
	 */
	public <M> M createSynchronizedMock(Class<M> interfaceToMock) {
		return this.createMock(interfaceToMock, true);
	}

	/**
	 * Creates the mock object.
	 * 
	 * @param <M>          Interface type.
	 * @param classToMock  {@link Class} to mock.
	 * @param isThreadSafe Flags whether to be thread safe.
	 * @return Mock object.
	 */
	private <M> M createMock(Class<M> classToMock, boolean isThreadSafe) {

		// Create the control
		IMocksControl mockControl = this.easyMockSupport.createStrictControl();
		if (isThreadSafe) {
			mockControl.makeThreadSafe(true);
		}

		// Obtain the mock object
		M mockObject = mockControl.createMock(classToMock);

		// Output details of mock
		if (this.logTestSupport != null) {
			this.logTestSupport.printMessage("mock '" + mockObject.getClass().getName() + "' is of class "
					+ classToMock.getSimpleName() + " [" + classToMock.getName() + "]");
		}

		// Register the mock object
		this.mockRegistry.put(mockObject, mockControl);

		// Return the mocked object
		return mockObject;
	}

	/**
	 * Wraps a parameter value when attempting to capture.
	 * 
	 * @param <T>   Value type.
	 * @param value Value.
	 * @return Value for parameter.
	 */
	public <T> T param(T value) {
		return EasyMock.eq(value);
	}

	/**
	 * Wraps a parameter type expected.
	 * 
	 * @param <T>  Value type.
	 * @param type Expected type.
	 * @return Value for parameter.
	 */
	public <T> T paramType(Class<T> type) {
		return EasyMock.isA(type);
	}

	/**
	 * Convenience method to record a method and its return on a mock object.
	 * 
	 * @param <T>            Expected result type.
	 * @param mockObject     Mock object.
	 * @param ignore         Result of operation on the mock object. This is only
	 *                       provided to obtain correct return type for recording
	 *                       return.
	 * @param recordedReturn Value that is recorded to be returned from the mock
	 *                       object.
	 */
	public <T> void recordReturn(Object mockObject, T ignore, T recordedReturn) {
		EasyMock.expect(ignore).andReturn(recordedReturn);
	}

	/**
	 * Convenience method to record a method, an {@link ArgumentsMatcher} and return
	 * value.
	 *
	 * @param <T>            Expected result type.
	 * @param mockObject     Mock object.
	 * @param ignore         Result of operation on the mock object. This is only
	 *                       provided to obtain correct return type for recording
	 *                       return.
	 * @param recordedReturn Value that is recorded to be returned from the mock
	 *                       object.
	 * @param matcher        {@link ArgumentsMatcher}.
	 */
	public <T> void recordReturn(Object mockObject, T ignore, T recordedReturn, ArgumentsMatcher matcher) {
		EasyMock.expect(ignore).andAnswer(() -> {
			Object[] arguments = EasyMock.getCurrentArguments();
			if (!matcher.matches(arguments)) {
				Assertions.fail("Invalid arguments: " + arguments);
			}
			return recordedReturn;
		});
	}

	/**
	 * Convenience method to record void method.
	 * 
	 * @param mockObject Mock object.
	 * @param matcher    {@link ArgumentsMatcher}.
	 */
	public void recordVoid(Object mockObject, ArgumentsMatcher matcher) {
		EasyMock.expectLastCall().andAnswer(() -> {
			Object[] arguments = EasyMock.getCurrentArguments();
			if (!matcher.matches(arguments)) {
				return Assertions.fail("Invalid arguments: " + arguments);
			}
			return null;
		});
	}

	/**
	 * Convenience method to record an {@link Exception}.
	 * 
	 * @param <T>        Expected result type.
	 * @param mockObject Mock object.
	 * @param ignore     Result of operation on the mock object. This is only
	 *                   provided to obtain correct return type for recording
	 *                   return.
	 * @param exception  {@link Throwable}.
	 */
	public <T> void recordThrows(Object mockObject, T ignore, Throwable exception) {
		EasyMock.expect(ignore).andThrow(exception);
	}

	/**
	 * Flags all the mock objects to replay.
	 */
	public void replayMockObjects() {
		this.easyMockSupport.replayAll();
	}

	/**
	 * Verifies all mock objects.
	 */
	public void verifyMockObjects() {
		this.easyMockSupport.verifyAll();
	}

	/**
	 * Test logic interface.
	 * 
	 * @param <R> Return type.
	 * @param <T> Possible {@link Throwable}.
	 */
	protected static interface TestLogic<R, T extends Throwable> {
		R run() throws T;
	}

	/**
	 * Undertakes test wrapping with mock object replay and verify.
	 * 
	 * @param <R>  Return type of test logic.
	 * @param <T>  Possible {@link Throwable}.
	 * @param test Test logic to wrap in replay/verify.
	 * @return Result of test logic.
	 * @throws T If logic throws {@link Exception}.
	 */
	protected <R, T extends Throwable> R doTest(TestLogic<R, T> test) throws T {
		this.replayMockObjects();
		R result = test.run();
		this.verifyMockObjects();
		return result;
	}

}
