/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.tutorial.pageflowhttpserver;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import net.officefloor.plugin.web.http.application.HttpSessionStateful;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Example logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: class
@HttpSessionStateful
public class TemplateLogic implements Serializable {

	private List<Item> items = new LinkedList<Item>();
	// END SNIPPET: class

	// START SNIPPET: flows
	@FlowInterface
	public static interface PageFlows {

		void getNoItems();

		void endListItems();
	}
	// END SNIPPET: flows

	// START SNIPPET: items
	public Item[] getItems() {
		return this.items.toArray(new Item[this.items.size()]);
	}
	// END SNIPPET: items

	// START SNIPPET: control
	public void getListItems(PageFlows flows) {
		if (this.items.size() == 0) {
			flows.getNoItems(); // skip to getNoItems
		}
	}

	public void getNoItems(PageFlows flows) {
		if (this.items.size() > 0) {
			flows.endListItems(); // skip to endListItems
		}
	}
	// END SNIPPET: control

	// START SNIPPET: addItem
	public void addItem(Item item) {
		this.items.add(item);
	}
	// END SNIPPET: addItem

	// START SNIPPET: clear
	public void clear() {
		this.items.clear();
	}
	// END SNIPPET: clear

	/**
	 * Inner class for extra dependency example. 
	 */
	public static class ExtraDependencyExample {

		// START SNIPPET: extraDependency
		public void getNoItems(PageFlows flows, DataSource dataSource) {
			// code requiring DataSource
		}
		// END SNIPPET: extraDependency
	}

}