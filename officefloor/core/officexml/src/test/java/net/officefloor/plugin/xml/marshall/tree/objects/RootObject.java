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
package net.officefloor.plugin.xml.marshall.tree.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Test object for marshalling into XML.
 * 
 * @author Daniel Sagenschneider
 */
public class RootObject {

	private final int depth;

	public RootObject() {
		this.depth = 0;
	}

	public RootObject(int depth) {
		this.depth = depth;
	}

	public boolean getBoolean() {
		return true;
	}

	public byte getByte() {
		return 1;
	}

	public char getChar() {
		return 'a';
	}

	public int getInt() {
		return 2;
	}

	public long getLong() {
		return 3;
	}

	public float getFloat() {
		return 4.4f;
	}

	public double getDouble() {
		return 5.5;
	}

	public String getString() {
		return "String Value";
	}

	public RootObject getChild() {
		return new RootObject(this.depth + 1);
	}

	public Collection<RootObject> getChildren() {
		List<RootObject> children = new ArrayList<RootObject>();
		for (int i = 0; i < 4; i++) {
			children.add(new RootObject(this.depth + 1));
		}
		return children;
	}

	public Object getGenericType() {
		return new RootObject(this.depth + 1);
	}

	public RootObject getRecursiveChild() {
		if (this.depth >= 2) {
			return null;
		} else {
			return new RootObject(this.depth + 1);
		}
	}

	@SuppressWarnings("unchecked")
	public Collection<RootObject> getRecursiveChildren() {
		if (this.depth >= 2) {
			return Collections.EMPTY_LIST;
		} else {
			List<RootObject> children = new ArrayList<RootObject>(1);
			children.add(new RootObject(this.depth + 1));
			children.add(new RootObject(this.depth + 1));
			return children;
		}
	}

}
