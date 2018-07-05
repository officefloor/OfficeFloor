/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
