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
package net.officefloor.polyglot.javascript;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * JavaScript function {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaScriptFunctionSectionSource extends ClassSectionSource {

	/**
	 * {@link Property} name for the JavaScript function name.
	 */
	public static final String PROPERTY_FUNCTION_NAME = JavaScriptManagedFunctionSource.PROPERTY_FUNCTION_NAME;

	/**
	 * Path to the script file.
	 */
	private String scriptPath;

	/**
	 * Name of the function.
	 */
	private String functionName;

	/*
	 * ================= ClassSectionSource =====================
	 */

	@Override
	protected Class<?> getSectionClass(String sectionClassName) throws Exception {
		this.scriptPath = sectionClassName;
		this.functionName = this.getContext().getProperty(PROPERTY_FUNCTION_NAME);
		return ScriptSection.class;
	}

	/**
	 * Script section {@link Class}.
	 */
	protected static class ScriptSection {
		public void script() {
		}
	}

	@Override
	protected ObjectForMethod loadObjectForMethod(Class<?> sectionClass) throws Exception {
		return null;
	}

	@Override
	protected FunctionNamespaceType loadFunctionNamespaceType(String namespace, Class<?> sectionClass) {
		PropertyList properties = this.getContext().createPropertyList();
		properties.addProperty(JavaScriptManagedFunctionSource.PROPERTY_JAVASCRIPT_PATH).setValue(this.scriptPath);
		properties.addProperty(JavaScriptManagedFunctionSource.PROPERTY_FUNCTION_NAME).setValue(this.functionName);
		return this.getContext().loadManagedFunctionType(namespace, JavaScriptManagedFunctionSource.class.getName(),
				properties);
	}

	@Override
	protected SectionFunctionNamespace adddSectionFunctionNamespace(String namespace, Class<?> sectionClass) {
		SectionFunctionNamespace functionNamespace = this.getDesigner().addSectionFunctionNamespace(namespace,
				JavaScriptManagedFunctionSource.class.getName());
		functionNamespace.addProperty(JavaScriptManagedFunctionSource.PROPERTY_JAVASCRIPT_PATH, this.scriptPath);
		functionNamespace.addProperty(JavaScriptManagedFunctionSource.PROPERTY_FUNCTION_NAME, this.functionName);
		return functionNamespace;
	}

}