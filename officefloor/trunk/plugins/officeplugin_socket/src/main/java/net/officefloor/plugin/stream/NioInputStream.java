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
package net.officefloor.plugin.stream;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>
 * Provides non-blocking {@link InputStream}.
 * <p>
 * Should an attempt be made to read more than the available bytes, a
 * {@link NoAvailableInputException} will be thrown.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class NioInputStream extends InputStream {

	/*
	 * ===================== InputStream ===========================
	 */

	@Override
	public abstract int read() throws IOException, NoAvailableInputException;

	@Override
	public abstract int available() throws IOException;
	
// TODO provide more efficient implementations of the below

//	@Override
//	public abstract int read(byte[] b) throws IOException,
//			NoAvailableInputException;

//	@Override
//	public abstract int read(byte[] b, int off, int len) throws IOException,
//			NoAvailableInputException;

//	@Override
//	public abstract long skip(long n) throws IOException,
//			NoAvailableInputException;

	
}