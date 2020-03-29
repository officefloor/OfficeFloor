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
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.plugin.section.clazz.FlowAnnotation;
import net.officefloor.plugin.section.clazz.ParameterAnnotation;
import net.officefloor.plugin.variable.VariableAnnotation;

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

		// Load managed function type for the procedure
		PropertyList typeProperties = context.createPropertyList();
		for (String propertyName : context.getPropertyNames()) {
			typeProperties.addProperty(propertyName).setValue(context.getProperty(propertyName));
		}
		typeProperties.addProperty(ProcedureManagedFunctionSource.RESOURCE_PROPERTY_NAME).setValue(className);
		typeProperties.addProperty(ProcedureManagedFunctionSource.SOURCE_NAME_PROPERTY_NAME).setValue(serviceName);
		typeProperties.addProperty(ProcedureManagedFunctionSource.PROCEDURE_PROPERTY_NAME).setValue(procedureName);
		ManagedFunctionType<?, ?> type = context
				.loadManagedFunctionType(FUNCTION_NAME, ProcedureManagedFunctionSource.class.getName(), typeProperties)
				.getManagedFunctionTypes()[0];

		// Load the procedure
		SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace(FUNCTION_NAME,
				ProcedureManagedFunctionSource.class.getName());
		for (String propertyName : context.getPropertyNames()) {
			namespace.addProperty(propertyName, context.getProperty(propertyName));
		}
		namespace.addProperty(ProcedureManagedFunctionSource.RESOURCE_PROPERTY_NAME, className);
		namespace.addProperty(ProcedureManagedFunctionSource.SOURCE_NAME_PROPERTY_NAME, serviceName);
		namespace.addProperty(ProcedureManagedFunctionSource.PROCEDURE_PROPERTY_NAME, procedureName);
		SectionFunction procedure = namespace.addSectionFunction(FUNCTION_NAME, procedureName);

		// Link objects
		ParameterAnnotation parameterAnnotation = type.getAnnotation(ParameterAnnotation.class);
		int parameterIndex = (parameterAnnotation != null) ? parameterAnnotation.getParameterIndex() : -1;
		String parameterType = null;
		ManagedFunctionObjectType<?>[] objectTypes = type.getObjectTypes();
		NEXT_OBJECT: for (int i = 0; i < objectTypes.length; i++) {
			ManagedFunctionObjectType<?> objectType = objectTypes[i];
			String objectName = objectType.getObjectName();
			String objectTypeName = objectType.getObjectType().getName();

			// Determine if parameter
			if (i == parameterIndex) {
				procedure.getFunctionObject(objectName).flagAsParameter();
				parameterType = objectTypeName;
				continue NEXT_OBJECT; // parameter
			}

			// Variable registered via augmentation
			String variableName = VariableAnnotation.extractPossibleVariableName(objectType);
			if (variableName != null) {
				continue NEXT_OBJECT; // not include variable
			}

			// Link as section object
			SectionObject sectionObject = designer.addSectionObject(objectName, objectTypeName);
			sectionObject.setTypeQualifier(objectType.getTypeQualifier());
			designer.link(procedure.getFunctionObject(objectName), sectionObject);
		}

		// Provide input to invoke procedure
		SectionInput sectionInput = designer.addSectionInput(ProcedureArchitect.INPUT_NAME, parameterType);
		designer.link(sectionInput, procedure);

		// Determine if next output
		if (isNext) {
			Class<?> returnType = type.getReturnType();
			String returnTypeName = (returnType != null) ? returnType.getName() : null;
			SectionOutput next = designer.addSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME, returnTypeName, false);
			designer.link(procedure, next);
		}

		// Link the flows
		FlowAnnotation[] flows = type.getAnnotation(FlowAnnotation[].class);
		if (flows != null) {
			// Load flows from annotation
			for (FlowAnnotation flow : flows) {
				String flowName = flow.getFlowName();
				Class<?> argumentType = flow.getParameterType();
				String argumentTypeName = (argumentType != null) ? argumentType.getName() : null;
				boolean isSpawn = flow.isSpawn();
				SectionOutput sectionOutput = designer.addSectionOutput(flowName, argumentTypeName, false);
				designer.link(procedure.getFunctionFlow(flowName), sectionOutput, isSpawn);
			}
		} else {
			// Load flows from type
			for (ManagedFunctionFlowType<?> flowType : type.getFlowTypes()) {
				String flowName = flowType.getFlowName();
				Class<?> argumentType = flowType.getArgumentType();
				String argumentTypeName = (argumentType != null) ? argumentType.getName() : null;
				boolean isSpawn = false; // must use annotation to indicate spawn
				SectionOutput sectionOutput = designer.addSectionOutput(flowName, argumentTypeName, false);
				designer.link(procedure.getFunctionFlow(flowName), sectionOutput, isSpawn);
			}
		}

		// Link the escalations
		for (ManagedFunctionEscalationType escalationType : type.getEscalationTypes()) {
			String escalationTypeName = escalationType.getEscalationType().getName();
			SectionOutput sectionOutput = designer.addSectionOutput(escalationTypeName,
					escalationType.getEscalationType().getName(), true);
			designer.link(procedure.getFunctionEscalation(escalationTypeName), sectionOutput, false);
		}
	}

}
