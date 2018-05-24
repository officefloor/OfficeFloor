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

import java.io.PrintWriter;
import java.io.Writer;

import net.officefloor.plugin.socket.server.http.HttpResponse;

/**
 * <p>
 * {@link HttpResponse} {@link PrintWriter}.
 * <p>
 * This forces flushing after every write.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResponsePrintWriter extends PrintWriter {

	/**
	 * Delegate {@link HttpResponseServletOutputStream}.
	 */
	private final HttpResponseServletOutputStream delegate;

	/**
	 * Initiate.
	 * 
	 * @param writer
	 *            {@link Writer} to send data.
	 * @param delegate
	 *            Delegate {@link HttpResponseServletOutputStream}.
	 */
	public HttpResponsePrintWriter(Writer writer,
			HttpResponseServletOutputStream delegate) {
		super(writer);
		this.delegate = delegate;
	}

	/*
	 * ==================== PrintWriter =======================
	 */

	@Override
	public void write(char[] buf, int off, int len) {
		try {
			this.delegate.bufferingContent();
			super.write(buf, off, len);
			this.flush();
		} finally {
			this.delegate.contentBuffered();
		}
	}

	@Override
	public void write(char[] buf) {
		try {
			this.delegate.bufferingContent();
			super.write(buf);
			this.flush();
		} finally {
			this.delegate.contentBuffered();
		}
	}

	@Override
	public void write(int c) {
		try {
			this.delegate.bufferingContent();
			super.write(c);
			this.flush();
		} finally {
			this.delegate.contentBuffered();
		}
	}

	@Override
	public void write(String s, int off, int len) {
		try {
			this.delegate.bufferingContent();
			super.write(s, off, len);
			this.flush();
		} finally {
			this.delegate.contentBuffered();
		}
	}

	@Override
	public void write(String s) {
		try {
			this.delegate.bufferingContent();
			super.write(s);
			this.flush();
		} finally {
			this.delegate.contentBuffered();
		}
	}

}