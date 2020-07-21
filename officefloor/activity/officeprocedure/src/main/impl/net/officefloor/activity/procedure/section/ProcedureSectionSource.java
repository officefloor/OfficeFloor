/*-
 * #%L
 * Procedure
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.activity.procedure.section;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.plugin.section.clazz.loader.ClassSectionLoader;
import net.officefloor.plugin.section.clazz.loader.ClassSectionLoaderContext;
import net.officefloor.plugin.section.clazz.loader.FunctionClassSectionLoaderContext;
import net.officefloor.plugin.section.clazz.loader.FunctionDecoration;

/**
 * {@link SectionSource} for {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureSectionSource extends AbstractSectionSource {

	/**
	 * Indicates if next {@link SectionOutput} should be configured.
	 */
	public static final String IS_NEXT_PROPERTY_NAME = "next";

	/*
	 * ==================== SectionSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(ProcedureManagedFunctionSource.RESOURCE_PROPERTY_NAME, "Class");
		context.addProperty(ProcedureManagedFunctionSource.SOURCE_NAME_PROPERTY_NAME, "Source");
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Obtain procedure details
		String className = context.getProperty(ProcedureManagedFunctionSource.RESOURCE_PROPERTY_NAME);
		String serviceName = context.getProperty(ProcedureManagedFunctionSource.SOURCE_NAME_PROPERTY_NAME);
		String procedureName = context.getSectionLocation();
		boolean isNext = Boolean.parseBoolean(context.getProperty(IS_NEXT_PROPERTY_NAME, Boolean.FALSE.toString()));

		// Name of the managed function
		final String FUNCTION_NAME = "procedure";

		// Load properties for the procedure
		PropertyList properties = context.createPropertyList();
		for (String propertyName : context.getPropertyNames()) {
			properties.addProperty(propertyName).setValue(context.getProperty(propertyName));
		}
		properties.addProperty(ProcedureManagedFunctionSource.RESOURCE_PROPERTY_NAME).setValue(className);
		properties.addProperty(ProcedureManagedFunctionSource.SOURCE_NAME_PROPERTY_NAME).setValue(serviceName);
		properties.addProperty(ProcedureManagedFunctionSource.PROCEDURE_PROPERTY_NAME).setValue(procedureName);

		// Create the class section loader
		ClassSectionLoader sectionLoader = new ClassSectionLoader(designer, context);

		// Load the procedure
		SectionFunction[] procedureCapture = new SectionFunction[] { null };
		ManagedFunctionType<?, ?>[] functionTypeCapture = new ManagedFunctionType[] { null };
		Class<?>[] parameterTypeCapture = new Class[] { null };
		sectionLoader.addManagedFunctions(FUNCTION_NAME, ProcedureManagedFunctionSource.class.getName(), properties,
				new FunctionDecoration() {

					@Override
					public String getFunctionName(ManagedFunctionType<?, ?> functionType,
							ClassSectionLoaderContext loaderContext) {
						return FUNCTION_NAME;
					}

					@Override
					public void decorateSectionFunction(FunctionClassSectionLoaderContext functionContext) {
						procedureCapture[0] = functionContext.getSectionFunction();
						functionTypeCapture[0] = functionContext.getManagedFunctionType();
						parameterTypeCapture[0] = functionContext.getParameterType();
					}
				});
		SectionFunction procedure = procedureCapture[0];
		ManagedFunctionType<?, ?> functionType = functionTypeCapture[0];
		Class<?> parameterType = parameterTypeCapture[0];

		// Provide input to invoke procedure
		SectionInput sectionInput = designer.addSectionInput(ProcedureArchitect.INPUT_NAME,
				parameterType == null ? null : parameterType.getName());
		designer.link(sectionInput, procedure);

		// Determine if next output
		if (isNext) {
			Class<?> returnType = functionType.getReturnType();
			String returnTypeName = (returnType != null) ? returnType.getName() : null;
			SectionOutput next = designer.addSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME, returnTypeName, false);
			designer.link(procedure, next);
		}
	}

}
