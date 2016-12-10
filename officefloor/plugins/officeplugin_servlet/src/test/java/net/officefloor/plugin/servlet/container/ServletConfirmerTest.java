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
package net.officefloor.plugin.servlet.container;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;

/**
 * Tests the {@link ServletConfirmerTest}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletConfirmerTest extends OfficeFrameTestCase {

	/**
	 * {@link ServletConfirmer} to test.
	 */
	private final ServletConfirmer confirmer = new ServletConfirmer();

	/**
	 * {@link HttpServletRequest} for recording.
	 */
	private final HttpServletRequest request = this.confirmer
			.getHttpServletRequestRecorder();

	/**
	 * {@link PrintStream} to ignore errors.
	 */
	private PrintStream err;

	@Override
	protected void setUp() throws Exception {
		// Ignore error output
		this.err = System.err;
		System.setErr(new PrintStream(new ByteArrayOutputStream()));

		// Provide blank line between each test
		System.out.println();
	}

	@Override
	protected void tearDown() throws Exception {
		System.setErr(this.err);
	}

	/**
	 * Ensure {@link ServletConfirmer} provides validation.
	 */
	public void testServletConfirmer() throws Exception {
		this.request.getRequestURI();
		assertEquals("Fails to confirm", "/test",
				this.confirmer.confirm("test"));
	}

	/**
	 * Validate method.
	 */
	public void test_getMethod() {
		this.confirm();
	}

	/**
	 * Validate method.
	 */
	public void test_getPathInfo() {
		this.confirm();
	}

	/**
	 * Validate method.
	 */
	public void test_getQueryString() {
		this.confirm();
	}

	/**
	 * Validate method.
	 */
	public void test_getRequestURI() {
		this.confirm();
	}

	/**
	 * Validate method.
	 */
	public void test_getRequestURL() {
		this.confirm();
	}

	/**
	 * Validates method.
	 */
	public void test_getContextPath() throws Exception {
		this.confirmer.setContextPath("/context");
		this.confirm("/context/path/");
	}

	/**
	 * Validate method.
	 */
	public void test_getServletPath() {
		this.confirmer.setContextPath("/context");
		this.confirmer.setServletPath("/servlet/*");
		this.confirm("/context/servlet/path");
	}

	/**
	 * Validate method.
	 */
	public void test_getPathTranslated() {
		this.confirmer.setContextPath("/context");
		this.confirmer.setServletPath("/servlet/*");
		this.confirm("/context/servlet/path/translated?query");
	}

	/**
	 * Validate method.
	 */
	public void test_getScheme() {
		this.confirm();
	}

	/**
	 * Validate method.
	 */
	public void test_getProtocol() {
		this.confirm();
	}

	/**
	 * Validate method.
	 */
	public void test_getParameterMap() {
		this.confirm();
	}

	/**
	 * Validates the date header.
	 */
	public void test_getDateHeader() throws Exception {
		this.confirmer.setProxyReturn(new Long(1));
		this.request.getDateHeader("date");
		this.output("getDateHeader", this.confirmer.confirm(null, "date",
				"Sun, 06 Nov 1994 08:49:37 GMT"));
	}

	/**
	 * Validate method.
	 */
	public void test_getHeaderNames() throws Exception {
		this.confirm("test", "one", "10", "one", "11");
	}

	/**
	 * Validates method.
	 */
	public void test_getHeaders() throws Exception {
		this.request.getHeaders("one");
		this.output("getHeaders",
				this.confirmer.confirm("test", "one", "10", "one", "11"));
	}

	/**
	 * Validates method.
	 */
	public void test_getContentLength() throws Exception {
		this.confirmer.setProxyReturn(new Integer(-10));
		this.confirm();
	}

	/**
	 * Validates method.
	 */
	public void test_getRemoteAddr() throws Exception {
		this.confirm();
	}

	/**
	 * Validates method.
	 */
	public void test_getRemoteHost() throws Exception {
		this.confirm();
	}

	/**
	 * Validates method.
	 */
	public void test_getRemotePort() throws Exception {
		this.confirmer.setProxyReturn(new Integer(-1));
		this.confirm();
	}

	/**
	 * Validates method.
	 */
	public void test_getLocalAddr() throws Exception {
		this.confirm();
	}

	/**
	 * Validates method.
	 */
	public void test_getLocalName() throws Exception {
		this.confirm();
	}

	/**
	 * Validates method.
	 */
	public void test_getLocalPort() throws Exception {
		this.confirmer.setProxyReturn(new Integer(-1));
		this.confirm();
	}

	/**
	 * Validates method.
	 */
	public void test_getServerName() throws Exception {
		this.confirm();
	}

	/**
	 * Validates method.
	 */
	public void test_getServerPort() throws Exception {
		this.confirmer.setProxyReturn(new Integer(-1));
		this.confirm();
	}

	/**
	 * Confirms the value from the {@link Method}.
	 * 
	 * @param uri
	 *            URI.
	 * @param headerNameValues
	 *            {@link HttpHeader} name values.
	 */
	private void confirm(String... uriThenHeaderNameValues) {
		try {
			// Obtain the method name
			final String prefix = "test_";
			String methodName = this.getName();
			assertTrue("Test name not following convention - " + methodName,
					methodName.startsWith(prefix));
			methodName = methodName.substring(prefix.length());

			// Obtain the URI
			String uri = (uriThenHeaderNameValues.length > 0 ? uriThenHeaderNameValues[0]
					: "/server/path?name=value&one=1+space;two=2%20space#fragment");

			// Obtain the header name values
			int headersLength = Math.max(0,
					(uriThenHeaderNameValues.length - 1));
			String[] headerNameValues = new String[headersLength];
			for (int i = 1; i < uriThenHeaderNameValues.length; i++) {
				headerNameValues[i - 1] = uriThenHeaderNameValues[i];
			}

			// Invoke the method
			Method method = this.request.getClass().getMethod(methodName);
			method.invoke(this.request);

			// Obtain the result
			Object result = this.confirmer.confirm(uri, headerNameValues);

			// Output result
			this.output(methodName, result);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Outputs the result.
	 * 
	 * @param methodName
	 *            {@link Method} name.
	 * @param result
	 *            Result.
	 */
	@SuppressWarnings("unchecked")
	private void output(String methodName, Object result) {
		System.out.print(methodName + "()=");
		if (result instanceof Enumeration<?>) {
			Enumeration<?> enumeration = (Enumeration<?>) result;
			while (enumeration.hasMoreElements()) {
				System.out.print(" " + enumeration.nextElement());
			}
		} else if (result instanceof Map<?, ?>) {
			Map<String, String[]> map = (Map<String, String[]>) result;
			for (String key : map.keySet()) {
				System.out.print(" " + key);
				String[] values = (String[]) map.get(key);
				for (String value : values) {
					System.out.print("," + value);
				}
			}
		} else {
			System.out.print(result);
		}
		System.out.println();
	}

}