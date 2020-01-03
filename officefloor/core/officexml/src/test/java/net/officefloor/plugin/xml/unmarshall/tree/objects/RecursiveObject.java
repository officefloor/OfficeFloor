package net.officefloor.plugin.xml.unmarshall.tree.objects;

/**
 * Recursive object to load.
 * 
 * @author Daniel Sagenschneider
 */
public class RecursiveObject {

	protected Person person;

	public Person getPerson() {
		return this.person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}
	
	protected ComplexParent complexParent;
	
	public ComplexParent getComplexParent() {
		return this.complexParent;
	}
	
	public void setComplexParent(ComplexParent complexParent) {
		this.complexParent = complexParent;
	}

	protected ManyChildren manyChildren;
	
	public ManyChildren getManyChildren() {
		return this.manyChildren;
	}
	
	public void setManyChildren(ManyChildren manyChildren) {
		this.manyChildren = manyChildren;
	}
}
