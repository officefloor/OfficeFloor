/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
	 */
	public String getClassName() {
		return this.name + "Model";
	}

	/**
	 * Event name.
	 */
	public String getEventName() {
		return this.name + "Event";
	}

	/**
	 * <code>true</code> if is a connection model.
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

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Package.
	 */
	private String packageName;

	public String getPackageName() {
		return this.packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	/**
	 * Import classes.
	 */
	private List<String> imports = new LinkedList<String>();

	public List<String> getImportClasses() {
		return this.imports;
	}

	public void addImportClass(String importClass) {
		this.imports.add(importClass);
	}

	/**
	 * Class suffix.
	 */
	private String classSuffix;

	public String getClassSuffix() {
		return (this.classSuffix == null ? "" : this.classSuffix);
	}

	public void setClassSuffix(String classSuffix) {
		this.classSuffix = classSuffix;
	}

	/**
	 * Interfaces to implement. These are typically marker interfaces without
	 * methods.
	 */
	private List<String> interfaces = new LinkedList<String>();

	public List<String> getInterfaces() {
		return this.interfaces;
	}

	public void addInterface(String interfaceName) {
		this.interfaces.add(interfaceName);
	}

	/**
	 * Fields.
	 */
	private List<FieldMetaData> fields = new LinkedList<FieldMetaData>();

	public List<FieldMetaData> getFields() {
		return this.fields;
	}

	public void addField(FieldMetaData field) {
		this.fields.add(field);
	}

	/**
	 * Lists.
	 */
	private List<ListMetaData> lists = new LinkedList<ListMetaData>();

	public List<ListMetaData> getLists() {
		return this.lists;
	}

	public void addList(ListMetaData list) {
		this.lists.add(list);
	}

}
