/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.stream.impl;

import java.util.NoSuchElementException;

/**
 * Sequence of <code>byte</code> values.
 * 
 * @author Daniel Sagenschneider
 */
public interface ByteSequence {

	/**
	 * Empty {@link ByteSequence}.
	 */
	public static final ByteSequence EMPTY = new ByteSequence() {

		@Override
		public byte byteAt(int index) {
			throw new NoSuchElementException();
		}

		@Override
		public int length() {
			return 0;
		}
	};

	/**
	 * Obtains the <code>byte</code> at the index.
	 * 
	 * @param index
	 *            Index of the <code>byte</code>.
	 * @return <code>byte</code> at the index.
	 */
	byte byteAt(int index);

	/**
	 * Obtains the number of <code>byte</code> values in the
	 * {@link ByteSequence}.
	 * 
	 * @return Number of <code>byte</code> values in the {@link ByteSequence}.
	 */
	int length();

}
