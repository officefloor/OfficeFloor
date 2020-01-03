package net.officefloor.plugin.xml.unmarshall.tree.objects;

/**
 * Complex child target object to be loaded.
 * 
 * @author Daniel Sagenschneider
 */
public class ComplexChild {

	protected String info;

	public String getInfo() {
		return this.info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	protected ComplexParent complexParent;
	
	public ComplexParent getComplexParent() {
		return this.complexParent;
	}
	
	public void setComplexParent(ComplexParent complexParent) {
		this.complexParent = complexParent;
	}

}
