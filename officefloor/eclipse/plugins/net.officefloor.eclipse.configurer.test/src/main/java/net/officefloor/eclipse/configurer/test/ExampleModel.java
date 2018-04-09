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
package net.officefloor.eclipse.configurer.test;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.model.Model;

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
	 * {@link Model} class name.
	 */
	public String modelClassName;

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

		public void write(PrintStream w) {
			w.print("    { text=");
			w.print(this.text);
			w.print(", flag=");
			w.print(this.flag);
			w.println(" }");
		}
	}

	public void write(PrintStream w) {

		// Load the details
		w.println("{ text=" + this.text);
		w.println(", flag=" + this.flag);
		w.println(", class=" + this.className);
		w.println(", model=" + this.modelClassName);
		w.println(", resource=" + this.resourceName);
		w.println(", choice=" + this.choiceValue);
		w.println(", list=[");
		for (ExampleItem item : this.items) {
			item.write(w);
		}
		w.println("], properties=[");
		for (Property property : this.properties) {
			w.print("    { name=");
			w.print(property.getName());
			w.print(", value=");
			w.print(property.getValue());
			w.println(" }");
		}
		w.print("], sources=[");
		for (String source : this.sources) {
			w.print(" ");
			w.print(source);
		}
		w.println(" ]");
		w.print(", targets=[");
		for (String target : this.targets) {
			w.print(" ");
			w.print(target);
		}
		w.println(" ]");
		w.print(", mapping={");
		for (String key : this.mapping.keySet()) {
			String value = this.mapping.get(key);
			w.print(" ");
			w.print(key);
			w.print("=");
			w.print(value);
		}
		w.println(" }");
		w.print(", multiple=[");
		for (ExampleItem item : this.multiple) {
			item.write(w);
		}
		w.println("]");
		w.println("}");
	}

}