/*-
 * #%L
 * Model Generator
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

package net.officefloor.model.generate.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Meta-data for the generation of a Model.
 * 
 * @author Daniel Sagenschneider
 */
public class ModelMetaData {

	/**
	 * Default constructor.
	 */
	public ModelMetaData() {
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param name
	 *            Name.
	 * @param packageName
	 *            Package name.
	 * @param classSuffix
	 *            Class suffix.
	 * @param imports
	 *            Imports.
	 * @param interfaces
	 *            Interfaces.
	 * @param fields
	 *            Fields.
	 * @param lists
	 *            Lists.
	 */
	public ModelMetaData(String name, String packageName, String classSuffix,
			String[] imports, String[] interfaces, FieldMetaData[] fields,
			ListMetaData[] lists) {
		this.name = name;
		this.packageName = packageName;
		for (String interfaceName : interfaces) {
			this.interfaces.add(interfaceName);
		}
		for (String model : imports) {
			this.imports.add(model);
		}
		for (FieldMetaData model : fields) {
			this.fields.add(model);
		}
		for (ListMetaData model : lists) {
			this.lists.add(model);
		}
	}

	/**
	 * Class name.
	 * 
	 * @return Class name.
	 */
	public String getClassName() {
		return this.name + "Model";
	}

	/**
	 * Event name.
	 * 
	 * @return Event name.
	 */
	public String getEventName() {
		return this.name + "Event";
	}

	/**
	 * <code>true</code> if is a connection model.
	 * 
	 * @return Indicating if a connection model.
	 */
	public boolean isConnectionModel() {
		// Determine if end field/list
		for (FieldMetaData field : this.fields) {
			if ((field.getEndField() != null) || (field.getEndList() != null)) {
				return true;
			}
		}

		// Not a connection
		return false;
	}

	/**
	 * Name of Model.
	 */
	private String name;

	/** @return Name. */
	public String getName() {
		return this.name;
	}

	/** @param name Name. */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Package.
	 */
	private String packageName;

	/** @return Package name. */
	public String getPackageName() {
		return this.packageName;
	}

	/** @param packageName Package name. */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	/**
	 * Import classes.
	 */
	private List<String> imports = new LinkedList<String>();

	/** @return Import classes. */
	public List<String> getImportClasses() {
		return this.imports;
	}

	/** @param importClass Import class to add. */
	public void addImportClass(String importClass) {
		this.imports.add(importClass);
	}

	/**
	 * Class suffix.
	 */
	private String classSuffix;

	/** @return Class suffix. */
	public String getClassSuffix() {
		return (this.classSuffix == null ? "" : this.classSuffix);
	}

	/** @param classSuffix Class suffix. */
	public void setClassSuffix(String classSuffix) {
		this.classSuffix = classSuffix;
	}

	/**
	 * Interfaces to implement. These are typically marker interfaces without
	 * methods.
	 */
	private List<String> interfaces = new LinkedList<String>();

	/** @return Interfaces. */
	public List<String> getInterfaces() {
		return this.interfaces;
	}

	/** @param interfaceName Interface name to add. */
	public void addInterface(String interfaceName) {
		this.interfaces.add(interfaceName);
	}

	/**
	 * Fields.
	 */
	private List<FieldMetaData> fields = new LinkedList<FieldMetaData>();

	/** @return Fields. */
	public List<FieldMetaData> getFields() {
		return this.fields;
	}

	/** @param field {@link FieldMetaData} to add. */
	public void addField(FieldMetaData field) {
		this.fields.add(field);
	}

	/**
	 * Lists.
	 */
	private List<ListMetaData> lists = new LinkedList<ListMetaData>();

	/** @return Lists. */
	public List<ListMetaData> getLists() {
		return this.lists;
	}

	/** @param list {@link ListMetaData} to add. */
	public void addList(ListMetaData list) {
		this.lists.add(list);
	}

}
