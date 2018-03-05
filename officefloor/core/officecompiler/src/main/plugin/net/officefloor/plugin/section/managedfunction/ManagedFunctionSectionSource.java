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
package net.officefloor.plugin.section.managedfunction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * {@link SectionSource} implementation that wraps a
 * {@link ManagedFunctionSource} to expose it as a section.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionSectionSource extends AbstractSectionSource {

	/**
	 * Name of property prefix to specify the index of the parameter on the
	 * {@link ManagedFunction}. The resulting parameter name and value are as:
	 * 
	 * <pre>
	 *      PROPERTY_PARAMETER_PREFIX + "FunctionName" = "index in objects for the parameter (starting at 1)"
	 * </pre>
	 */
	public static final String PROPERTY_PARAMETER_PREFIX = "parameter.index.prefix.";

	/**
	 * Name of property specifying a comma separated list of {@link ManagedFunction}
	 * names that will have a {@link SectionOutput} created and linked as next.
	 */
	public static final String PROPERTY_FUNCTIONS_NEXT_TO_OUTPUTS = "functions.next.to.outputs";

	/*
	 * ====================== SectionSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Obtain the managed function source name
		String managedFunctionSourceName = context.getSectionLocation();

		// Obtain the properties
		PropertyList properties = context.createPropertyList();
		for (String name : context.getPropertyNames()) {
			String value = context.getProperty(name);
			properties.addProperty(name).setValue(value);
		}

		// Obtain the namespace type
		String functionNamespace = "NAMESPACE";
		FunctionNamespaceType namespaceType = context.loadManagedFunctionType(functionNamespace,
				managedFunctionSourceName, properties);

		// Add the namespace
		SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace(functionNamespace,
				managedFunctionSourceName);
		for (Property property : properties) {
			namespace.addProperty(property.getName(), property.getValue());
		}

		// Determine the functions which have next outputs
		Set<String> functionsWithNextOutput = new HashSet<String>();
		String nextFunctionsText = context.getProperty(PROPERTY_FUNCTIONS_NEXT_TO_OUTPUTS, "");
		for (String nextFunctionName : nextFunctionsText.split(",")) {
			functionsWithNextOutput.add(nextFunctionName.trim());
		}

		// Add the functions
		Map<String, SectionObject> sectionObjects = new HashMap<String, SectionObject>();
		Map<String, SectionOutput> sectionOutputs = new HashMap<String, SectionOutput>();
		for (ManagedFunctionType<?, ?> functionType : namespaceType.getManagedFunctionTypes()) {

			// Obtain the function name
			String functionName = functionType.getFunctionName();

			// Add the function
			SectionFunction function = namespace.addSectionFunction(functionName, functionName);

			// Determine the index of the parameter
			int parameterIndex = Integer.parseInt(context.getProperty(PROPERTY_PARAMETER_PREFIX + functionName, "-1"));

			// Link objects and flag parameter
			String parameterType = null;
			int objectIndex = 1;
			for (ManagedFunctionObjectType<?> objectType : functionType.getObjectTypes()) {

				// Obtain object details
				String objectName = objectType.getObjectName();
				String objectClassName = objectType.getObjectType();
				String typeQualifier = objectType.getTypeQualifier();

				// Determine the section object name
				String sectionObjectName = (typeQualifier == null ? "" : typeQualifier + "-") + objectClassName;

				// Obtain the object
				FunctionObject object = function.getFunctionObject(objectName);

				// Determine if parameter
				if (objectIndex == parameterIndex) {
					// Flag as parameter
					parameterType = objectClassName;
					object.flagAsParameter();

				} else {
					// Obtain the section object
					SectionObject sectionObject = sectionObjects.get(sectionObjectName);
					if (sectionObject == null) {
						sectionObject = designer.addSectionObject(sectionObjectName, objectClassName);
						if (typeQualifier != null) {
							sectionObject.setTypeQualifier(typeQualifier);
						}
						sectionObjects.put(sectionObjectName, sectionObject);
					}

					// Specify as section object dependency
					designer.link(object, sectionObject);
				}

				// Increment object index for next iteration
				objectIndex++;
			}

			// Link next output (if configured)
			if (functionsWithNextOutput.contains(functionName)) {

				// Obtain the section output
				SectionOutput sectionOutput = sectionOutputs.get(functionName);
				if (sectionOutput == null) {
					sectionOutput = designer.addSectionOutput(functionName, functionType.getReturnType(), false);
					sectionOutputs.put(functionName, sectionOutput);
				}

				// Link function to next output
				designer.link(function, sectionOutput);
			}

			// Link function flows to section outputs
			for (ManagedFunctionFlowType<?> flowType : functionType.getFlowTypes()) {

				// Obtain the flow details
				String flowName = flowType.getFlowName();
				String argumentType = flowType.getArgumentType();

				// Obtain the flow
				FunctionFlow flow = function.getFunctionFlow(flowName);

				// Obtain the section output
				SectionOutput sectionOutput = sectionOutputs.get(flowName);
				if (sectionOutput == null) {
					sectionOutput = designer.addSectionOutput(flowName, argumentType, false);
					sectionOutputs.put(flowName, sectionOutput);
				}

				// Link flow to output
				designer.link(flow, sectionOutput, false);
			}

			// Link function escalations to section outputs
			for (ManagedFunctionEscalationType escalationType : functionType.getEscalationTypes()) {

				// Obtain the escalation type
				String escalationTypeName = escalationType.getEscalationType();

				// Obtain the escalation
				FunctionFlow flow = function.getFunctionEscalation(escalationTypeName);

				// Obtain the section output
				String outputName = escalationTypeName;
				SectionOutput sectionOutput = sectionOutputs.get(outputName);
				if (sectionOutput == null) {
					sectionOutput = designer.addSectionOutput(outputName, escalationTypeName, true);
					sectionOutputs.put(outputName, sectionOutput);
				}

				// Link the escalation to output
				designer.link(flow, sectionOutput, false);
			}

			// Link function for input
			SectionInput input = designer.addSectionInput(functionName, parameterType);
			designer.link(input, function);
		}
	}

}