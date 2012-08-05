/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.stream.servlet;

import java.io.IOException;
import java.io.InputStream;

import net.officefloor.plugin.stream.BrowseInputStream;
import net.officefloor.plugin.stream.NoAvailableInputException;
import net.officefloor.plugin.stream.ServerInputStream;
import net.officefloor.plugin.stream.impl.ServerInputStreamImpl;

/**
 * {@link ServerInputStream} implementation that wraps an {@link InputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletServerInputStream extends ServerInputStreamImpl {

	/**
	 * {@link InputStream}.
	 */
	private final InputStream inputStream;

	/**
	 * {@link ServletBrowseInputStream}.
	 */
	private ServletBrowseInputStream browseInputStream = null;

	/**
	 * Initiate.
	 * 
	 * @param inputStream
	 *            {@link InputStream} being wrapped for
	 *            {@link ServerInputStreams} functionality.
	 */
	public ServletServerInputStream(InputStream inputStream) {
		super(inputStream);
		this.inputStream = inputStream;
		this.browseInputStream = new ServletBrowseInputStream(this.inputStream,
				1024, this);
	}

	/*
	 * ============================ ServerInputStream ==========================
	 */

	@Override
	public BrowseInputStream createBrowseInputStream() {
		// TODO implement ServletServerInputStream.createBrowseInputStream
		throw new UnsupportedOperationException(
				"TODO implement ServletServerInputStream.createBrowseInputStream");
	}

	@Override
	public int read() throws IOException, NoAvailableInputException {
		// TODO implement ServletServerInputStream.read
		throw new UnsupportedOperationException(
				"TODO implement ServletServerInputStream.read");
	}

	@Override
	public int available() throws IOException {
		// TODO implement ServletServerInputStream.available
		throw new UnsupportedOperationException(
				"TODO implement ServletServerInputStream.available");
	}

	@Override
	public int read(byte[] b) throws IOException {
		// TODO implement ServletServerInputStream.read
		throw new UnsupportedOperationException(
				"TODO implement ServletServerInputStream.read");
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		// TODO implement ServletServerInputStream.read
		throw new UnsupportedOperationException(
				"TODO implement ServletServerInputStream.read");
	}

	@Override
	public long skip(long n) throws IOException {
		// TODO implement ServletServerInputStream.skip
		throw new UnsupportedOperationException(
				"TODO implement ServletServerInputStream.skip");
	}

	@Override
	public void close() throws IOException {
		// TODO implement ServletServerInputStream.close
		throw new UnsupportedOperationException(
				"TODO implement ServletServerInputStream.close");
	}

}