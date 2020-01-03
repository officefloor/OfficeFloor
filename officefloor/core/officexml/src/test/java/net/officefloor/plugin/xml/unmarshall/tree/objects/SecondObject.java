package net.officefloor.plugin.xml.unmarshall.tree.objects;

/**
 * Second target object to be loaded.
 * 
 * @author Daniel Sagenschneider
 */
public class SecondObject {

	protected String details;

	public String getDetails() {
		return this.details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	protected String value;

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	protected ThirdObject thirdObject;

	public ThirdObject getThird() {
		return this.thirdObject;
	}

	public void setThird(ThirdObject thirdObject) {
		this.thirdObject = thirdObject;
	}

}
