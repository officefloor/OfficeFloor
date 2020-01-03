package net.officefloor.plugin.xml.unmarshall.tree.objects;

/**
 * Person to be loaded.
 * 
 * @author Daniel Sagenschneider
 */
public class Person {

	protected String position;

	public String getPosition() {
		return this.position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	protected Person person;

	public Person getPerson() {
		return this.person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}
}
