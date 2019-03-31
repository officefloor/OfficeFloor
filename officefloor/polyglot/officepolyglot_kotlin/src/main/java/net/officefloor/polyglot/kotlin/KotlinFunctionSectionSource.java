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
package net.officefloor.polyglot.kotlin;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Kotlin function {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class KotlinFunctionSectionSource extends ClassSectionSource {

	/**
	 * {@link Property} name of the Kotlin function to use.
	 */
	public static final String PROPERTY_FUNCTION_NAME = "function.name";

	/**
	 * Name of {@link ManagedFunction} to include.
	 */
	private String functionName;

	/*
	 * ==================== ClassSectionSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_FUNCTION_NAME, "Function");
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Obtain the function name
		this.functionName = context.getProperty(PROPERTY_FUNCTION_NAME);

		// Source the function
		super.sourceSection(designer, context);
	}

	@Override
	protected Class<?> getSectionClass(String sectionClassName) throws Exception {

		// Ensure kotlin functions
		if (!sectionClassName.endsWith("Kt")) {
			this.getDesigner()
					.addIssue("Class " + sectionClassName + " is not top level Kotlin functions (should end in Kt)");
			return null;
		}

		// Load the class
		return super.getSectionClass(sectionClassName);
	}

	@Override
	protected ObjectForMethod loadObjectForMethod(Class<?> sectionClass) throws Exception {
		return null; // no object
	}

	@Override
	protected FunctionNamespaceType loadFunctionNamespaceType(String namespace, Class<?> sectionClass) {
		PropertyList workProperties = this.getContext().createPropertyList();
		workProperties.addProperty(KotlinManagedFunctionSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(sectionClass.getName());
		return this.getContext().loadManagedFunctionType(namespace, KotlinManagedFunctionSource.class.getName(),
				workProperties);
	}

	@Override
	protected SectionFunctionNamespace adddSectionFunctionNamespace(String namespace, Class<?> sectionClass) {
		SectionFunctionNamespace functionNamespace = this.getDesigner().addSectionFunctionNamespace(namespace,
				KotlinManagedFunctionSource.class.getName());
		functionNamespace.addProperty(KotlinManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, sectionClass.getName());
		return functionNamespace;
	}

	@Override
	protected boolean isIncludeManagedFunctionType(ManagedFunctionType<?, ?> functionType) {
		return (this.functionName.equals(functionType.getFunctionName()));
	}

}