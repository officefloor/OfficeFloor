/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.impl.adapt;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * {@link InputStream} adapter.
 * 
 * @author Daniel Sagenschneider
 */
public class InputStreamAdapter extends InputStream {

	/**
	 * {@link InputStream} implementation.
	 */
	private final Object implementation;

	/**
	 * {@link ClassLoader} for the client.
	 */
	private final ClassLoader clientClassLoader;

	/**
	 * {@link ClassLoader} for the implementation.
	 */
	private final ClassLoader implClassLoader;

	/**
	 * Initiate.
	 * 
	 * @param implementation
	 *            {@link InputStream} implementation.
	 * @param clientClassLoader
	 *            {@link ClassLoader} of the client.
	 * @param implClassLoader
	 *            {@link ClassLoader} of the implementation.
	 */
	public InputStreamAdapter(Object implementation,
			ClassLoader clientClassLoader, ClassLoader implClassLoader) {
		this.implementation = implementation;
		this.clientClassLoader = clientClassLoader;
		this.implClassLoader = implClassLoader;
	}

	/**
	 * Invokes the method.
	 * 
	 * @param methodName
	 *            Name of the {@link Method}.
	 * @param arguments
	 *            Arguments for the {@link Method}.
	 * @param paramTypes
	 *            Parameter types.
	 * @return Return on the value.
	 */
	private Object invokeMethod(String methodName, Object[] arguments,
			Class<?>... paramTypes) {
		return TypeAdapter.invokeNoExceptionMethod(this.implementation,
				methodName, (arguments == null ? new Object[0] : arguments),
				paramTypes, this.clientClassLoader, this.implClassLoader);
	}

	/**
	 * Transforms the value into an <code>int</code>.
	 * 
	 * @param value
	 *            Value.
	 * @return <code>int</code> value.
	 */
	private int integer(Object value) {
		return ((Integer) value).intValue();
	}

	/*
	 * ================= InputStream =========================
	 */

	@Override
	public int read() throws IOException {
		return integer(invokeMethod("read", null));
	}

	@Override
	public int read(byte[] b) throws IOException {
		return integer(invokeMethod("read", new Object[] { b }, byte[].class));
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return integer(invokeMethod("read", new Object[] { b, off, len },
				byte[].class, int.class, int.class));
	}

	@Override
	public long skip(long n) throws IOException {
		return ((Long) invokeMethod("skip", new Object[] { n }, long.class))
				.longValue();
	}

	@Override
	public int available() throws IOException {
		return integer(invokeMethod("available", null));
	}

	@Override
	public void close() throws IOException {
		invokeMethod("close", null);
	}

	@Override
	public synchronized void mark(int readlimit) {
		invokeMethod("mark", new Object[] { readlimit }, int.class);
	}

	@Override
	public synchronized void reset() throws IOException {
		invokeMethod("reset", null);
	}

	@Override
	public boolean markSupported() {
		return ((Boolean) invokeMethod("markSupported", null)).booleanValue();
	}

}
