/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.polyglot.test;

/**
 * Primitive types.
 * 
 * @author Daniel Sagenschneider
 */
public class PrimitiveTypes {

	private final byte _byte;

	private final short _short;

	private final char _char;

	private final int _int;

	private final long _long;

	private final float _float;

	private final double _double;

	public PrimitiveTypes(byte _byte, short _short, char _char, int _int, long _long, float _float, double _double) {
		super();
		this._byte = _byte;
		this._short = _short;
		this._char = _char;
		this._int = _int;
		this._long = _long;
		this._float = _float;
		this._double = _double;
	}

	public byte getByte() {
		return _byte;
	}

	public short getShort() {
		return _short;
	}

	public char getChar() {
		return _char;
	}

	public int getInt() {
		return _int;
	}

	public long getLong() {
		return _long;
	}

	public float getFloat() {
		return _float;
	}

	public double getDouble() {
		return _double;
	}

}