/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.polyglot.scala;

import java.lang.reflect.Field;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.SectionClassManagedFunctionSource;

/**
 * Scala function {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ScalaFunctionSectionSource extends ClassSectionSource {

	/**
	 * {@link Property} name of the Scala function to use.
	 */
	public static final String PROPERTY_FUNCTION_NAME = "function.name";

	/*
	 * ==================== ClassSectionSource ==========================
	 */

	@Override
	protected Class<?> getSectionClass(String sectionClassName) throws Exception {

		// Load the class
		Class<?> objectClass = super.getSectionClass(sectionClassName);

		// Determine if Scala object (to obtain function)
		final String MODULE$_FIELD_NAME = "MODULE$";
		for (Field field : objectClass.getFields()) {
			if (MODULE$_FIELD_NAME.equals(field.getName())) {

				// Found module for functions (so return it's type)
				return field.getType();
			}
		}

		// As here, not Scala object
		this.getDesigner().addIssue("Class " + sectionClassName + " is not Scala Object (expecting "
				+ MODULE$_FIELD_NAME + " static field)");
		return null;
	}

	@Override
	protected ObjectForMethod loadObjectForMethod(Class<?> sectionClass) throws Exception {
		return null; // no object
	}

	@Override
	protected FunctionNamespaceType loadFunctionNamespaceType(String namespace, Class<?> sectionClass) {
		PropertyList workProperties = this.getContext().createPropertyList();
		workProperties.addProperty(ScalaManagedFunctionSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(sectionClass.getName());
		return this.getContext().loadManagedFunctionType(namespace, ScalaManagedFunctionSource.class.getName(),
				workProperties);
	}

	@Override
	protected SectionFunctionNamespace adddSectionFunctionNamespace(String namespace, Class<?> sectionClass) {
		SectionFunctionNamespace functionNamespace = this.getDesigner().addSectionFunctionNamespace(namespace,
				ScalaManagedFunctionSource.class.getName());
		functionNamespace.addProperty(SectionClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				sectionClass.getName());
		return functionNamespace;
	}

}