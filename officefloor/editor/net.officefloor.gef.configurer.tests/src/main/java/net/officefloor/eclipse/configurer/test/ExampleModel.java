/*-
 * #%L
 * net.officefloor.gef.configurer.tests
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
	public String text = "T";
	
	/**
	 * Multi-line text.
	 */
	public String multilineText = "One\nTwo\nThree\nFour";

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
	 * Items for selection.
	 */
	public List<ExampleItem> selections = Arrays.asList(new ExampleItem("Selection One", false),
			new ExampleItem("Selection Two", true));

	/**
	 * Selected item.
	 */
	public ExampleItem selectedItem = this.selections.get(0);

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
		 * @param text Text.
		 * @param flag Flag.
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
