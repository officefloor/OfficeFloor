/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.gwt.service;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Map;

import org.junit.Test;

import net.officefloor.frame.test.OfficeFrameTestCase;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Test extracting the {@link AsyncCallback} {@link ParameterizedType} raw
 * {@link Class}.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtAsyncMethodMetaDataTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to extract details from simple method.
	 */
	@Test
	public void testSimple() {
		this.doTest(null, String.class);
	}

	public void simple(AsyncCallback<String> callback) {
	}

	/**
	 * Ensure able to extract details with a parameter.
	 */
	@Test
	public void testParameter() {
		this.doTest(Long.class, Integer.class);
	}

	public void parameter(Long parameter, AsyncCallback<Integer> callback) {
	}

	/**
	 * Ensure able to extract primitive array return type.
	 */
	@Test
	public void testReturnPrimitiveArray() {
		this.doTest(null, byte[].class);
	}

	public void returnPrimitiveArray(AsyncCallback<byte[]> callback) {
	}

	/**
	 * Ensure able to extract primitive array return type.
	 */
	@Test
	public void testReturnObjectArray() {
		this.doTest(null, Integer[].class);
	}

	public void returnObjectArray(AsyncCallback<Integer[]> callback) {
	}

	/**
	 * Ensure able to extract raw return type.
	 */
	public void testRawReturn() {
		this.doTest(null, Object.class);
	}

	@SuppressWarnings("rawtypes")
	public void rawReturn(AsyncCallback callback) {
	}

	/**
	 * Ensure able to extract {@link ParameterizedType} return type.
	 */
	@Test
	public void testParameterizedReturnType() {
		this.doTest(null, Map.class);
	}

	public void parameterizedReturnType(
			AsyncCallback<Map<String, Object>> callback) {
	}

	/**
	 * Ensure error if try to use wildcard return type.
	 */
	@Test
	public void testWildcardExtends() {
		this.doTest("Return type can not be a wildcard - "
				+ AsyncCallback.class.getName()
				+ "<? extends java.lang.String>");
	}

	public void wildcardExtends(AsyncCallback<? extends String> callback) {
	}

	/**
	 * Ensure error if try to use wildcard return type.
	 */
	@Test
	public void testWildcardSuper() {
		this.doTest("Return type can not be a wildcard - "
				+ AsyncCallback.class.getName() + "<? super java.lang.String>");
	}

	public void wildcardSuper(AsyncCallback<? super String> callback) {
	}

	/**
	 * Ensure error if try to use wildcard return type.
	 */
	@Test
	public void testWildcardType() {
		this.doTest("Return type can not be a wildcard - "
				+ AsyncCallback.class.getName() + "<T>");
	}

	public <T extends Object> void wildcardType(AsyncCallback<T> callback) {
	}

	/**
	 * Ensure error if try to use wildcard return type.
	 */
	@Test
	public void testWildcardArray() {
		this.doTest("Return type can not be a wildcard - "
				+ AsyncCallback.class.getName() + "<T[]>");
	}

	public <T extends Object> void wildcardArray(AsyncCallback<T[]> callback) {
	}

	/**
	 * Ensure error if no parameters.
	 */
	@Test
	public void testNoParameters() {
		this.doTest("Method signature must be void noParameters([X parameter,] AsyncCallback<Y> callback)");
	}

	public void noParameters() {
	}

	/**
	 * Ensure error if no too many parameters.
	 */
	@Test
	public void testTooManyParameters() {
		this.doTest("Method signature must be void tooManyParameters([X parameter,] AsyncCallback<Y> callback)");
	}

	public void tooManyParameters(String one, String two,
			AsyncCallback<String> callback) {
	}

	/**
	 * Ensure error if no {@link AsyncCallback} parameter.
	 */
	@Test
	public void testNoAsyncCallbackParameter() {
		this.doTest("Last parameter must be AsyncCallback");
	}

	public void noAsyncCallbackParameter(Integer parameter) {
	}

	/**
	 * Ensure error if {@link AsyncCallback} is not last parameter.
	 */
	@Test
	public void testAsyncCallbackNotLast() {
		this.doTest("Last parameter must be AsyncCallback");
	}

	public void asyncCallbackNotLast(AsyncCallback<String> callback,
			Integer parameter) {
	}

	/**
	 * Ensure error if multiple {@link AsyncCallback} parameters.
	 */
	@Test
	public void testMultipleAsyncCallbacks() {
		this.doTest("May only have one AsyncCallback parameter");
	}

	public void multipleAsyncCallbacks(AsyncCallback<String> one,
			AsyncCallback<String> two) {
	}

	/*
	 * ---------------------- Test support methods -----------------------
	 */

	/**
	 * Convenience method to test successful extraction.
	 * 
	 * @param parameterType
	 *            Expected parameter type.
	 * @param returnType
	 *            Expected return type.
	 */
	private void doTest(Class<?> parameterType, Class<?> returnType) {
		this.doTest(parameterType, returnType, null);
	}

	/**
	 * Convenience method to test failed extraction.
	 * 
	 * @param errorMessage
	 *            Expected error message.
	 */
	private void doTest(String errorMessage) {
		this.doTest(null, null, errorMessage);
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param parameterType
	 *            Expected parameter type.
	 * @param returnType
	 *            Expected return type.
	 * @param errorMessage
	 *            Expected error message.
	 */
	private void doTest(Class<?> parameterType, Class<?> returnType,
			String errorMessage) {

		// Obtain the method name
		String testMethodName = this.getName();
		String methodName = testMethodName.substring("test".length());
		methodName = methodName.substring(0, 1).toLowerCase()
				+ methodName.substring(1);

		// Find the method
		Method method = null;
		for (Method check : this.getClass().getMethods()) {
			if (check.getName().equals(methodName)) {
				method = check; // found method
			}
		}
		assertNotNull("Can not find method " + methodName, method);

		// Extract the async method details
		GwtAsyncMethodMetaData metaData = new GwtAsyncMethodMetaData(method);

		// Validate async method details
		assertEquals("Incorrect method name", methodName,
				metaData.getMethodName());
		if (errorMessage == null) {
			// Validate successfully extracted details
			assertEquals("Incorrect parameter type", parameterType,
					metaData.getParameterType());
			assertEquals("Incorrect return type", returnType,
					metaData.getReturnType());
			assertNull("Should not have error", metaData.getError());
		} else {
			// Validate failed to extract details
			assertNull("Should not have parameter type on error",
					metaData.getParameterType());
			assertNull("Should not have return type on error",
					metaData.getReturnType());
			assertEquals("Incorrect error", errorMessage, metaData.getError());
		}
	}

}