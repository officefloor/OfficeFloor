/*-
 * #%L
 * OfficeXml
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
