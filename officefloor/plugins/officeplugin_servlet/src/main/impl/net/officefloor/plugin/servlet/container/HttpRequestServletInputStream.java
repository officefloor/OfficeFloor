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

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

import net.officefloor.plugin.socket.server.http.HttpRequest;

/**
 * {@link HttpRequest} {@link ServletInputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestServletInputStream extends ServletInputStream {

	/**
	 * Delegate {@link InputStream}.
	 */
	private final InputStream delegate;

	/**
	 * Initiate.
	 * 
	 * @param delegate
	 *            Delegate {@link InputStream}.
	 */
	public HttpRequestServletInputStream(InputStream delegate) {
		this.delegate = delegate;
	}

	/*
	 * ======================= ServletInputStream ===========================
	 */

	@Override
	public int read() throws IOException {
		return this.delegate.read();
	}

	@Override
	public int available() throws IOException {
		return this.delegate.available();
	}

	@Override
	public void close() throws IOException {
		this.delegate.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		this.delegate.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return this.delegate.markSupported();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return this.delegate.read(b, off, len);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return this.delegate.read(b);
	}

	@Override
	public synchronized void reset() throws IOException {
		this.delegate.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return this.delegate.skip(n);
	}

	/*
	 * ------------------ Servlet 3.x methods ----------------------
	 */

	@Override
	public boolean isFinished() {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public boolean isReady() {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public void setReadListener(ReadListener readListener) {
		UnsupportedHttpServletMethodException.notSupported();
	}

}