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
