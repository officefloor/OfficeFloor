/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
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
