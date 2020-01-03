package net.officefloor.plugin.xml.unmarshall.tree.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * First target object to be loaded.
 * 
 * @author Daniel Sagenschneider
 */
public class FirstObject {

	protected String info;

	public String getInfo() {
		return this.info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	protected SecondObject second;

	public SecondObject getSecond() {
		return this.second;
	}

	public void setSecond(SecondObject second) {
		this.second = second;
	}

	protected List<FourthObject> fourthObjects = new ArrayList<FourthObject>();

	public FourthObject[] getFourths() {
		return this.fourthObjects.toArray(new FourthObject[0]);
	}

	public void addFourth(FourthObject fourth) {
		this.fourthObjects.add(fourth);
	}
}
