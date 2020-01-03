package net.officefloor.plugin.xml.unmarshall.tree.objects;

/**
 * Complex Parent target object to be loaded.
 * 
 * @author Daniel Sagenschneider
 */
public class ComplexParent {

	protected String info;

	public String getInfo() {
		return this.info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
	
	protected ComplexChild complexChild;
	
	public ComplexChild getComplexChild() {
		return this.complexChild;
	}
	
	public void setComplexChild(ComplexChild complexChild) {
		this.complexChild = complexChild;
	}
}
