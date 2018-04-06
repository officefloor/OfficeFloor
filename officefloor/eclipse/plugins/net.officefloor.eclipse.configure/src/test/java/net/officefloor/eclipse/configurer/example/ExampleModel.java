/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.configurer.example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.properties.PropertyList;

/**
 * Example model to be configured.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleModel {

	/**
	 * Text.
	 */
	public String text = "TEXT";

	/**
	 * Flag.
	 */
	public boolean flag = true;

	/**
	 * Class name.
	 */
	public String className = Object.class.getName();

	/**
	 * Resource name.
	 */
	public String resourceName = "net/officefloor/example/Resource.txt";

	/**
	 * Choice value.
	 */
	public String choiceValue;

	/**
	 * Items.
	 */
	public List<ExampleItem> items = Arrays.asList(new ExampleItem("ITEM", true), new ExampleItem("ANOTHER", false));

	/**
	 * {@link PropertyList}.
	 */
	public PropertyList properties = new PropertyListImpl("one", "first", "two", "second");

	/**
	 * Sources.
	 */
	public List<String> sources = Arrays.asList("one", "two", "three");

	/**
	 * Targets.
	 */
	public List<String> targets = Arrays.asList("target", "another");

	/**
	 * Initial mappings.
	 */
	public Map<String, String> mapping = new HashMap<String, String>();

	/**
	 * Multiple.
	 */
	public List<ExampleItem> multiple = Arrays.asList(new ExampleItem("ONE", true), new ExampleItem("TWO", false));

	/**
	 * Instantiate.
	 */
	public ExampleModel() {
		this.mapping.put("one", "target");
	}

	/**
	 * Example item.
	 */
	public static class ExampleItem {

		/**
		 * Text.
		 */
		public String text;

		/**
		 * Flag.
		 */
		public boolean flag;

		/**
		 * Instantiate.
		 * 
		 * @param text
		 *            Text.
		 * @param flag
		 *            Flag.
		 */
		public ExampleItem(String text, boolean flag) {
			this.text = text;
			this.flag = flag;
		}
	}
}