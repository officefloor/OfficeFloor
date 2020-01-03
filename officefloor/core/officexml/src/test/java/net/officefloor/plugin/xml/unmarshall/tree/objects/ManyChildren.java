package net.officefloor.plugin.xml.unmarshall.tree.objects;

import java.util.LinkedList;
import java.util.List;

/**
 * Many children target object to be loaded.
 * 
 * @author Daniel Sagenschneider
 */
public class ManyChildren {
	
	protected String name;
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	protected List<ManyChildren> children = new LinkedList<ManyChildren>();

	public ManyChildren[] getChildren() {
		return children.toArray(new ManyChildren[0]);
	}

	public void addChild(ManyChildren child) {
		this.children.add(child);
	}
}
