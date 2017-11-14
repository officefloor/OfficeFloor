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
package net.officefloor.plugin.web.template.section;

import java.lang.reflect.Array;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.plugin.web.template.parse.HttpTemplate;

/**
 * Iterates over the array objects sending them to the {@link HttpTemplate} for
 * rendering.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateArrayIteratorManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * Name of property for the array component type.
	 */
	public static final String PROPERTY_COMPONENT_TYPE_NAME = "component.type.name";

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	public static final String FUNCTION_NAME = "iterate";

	/**
	 * Name of the {@link FunctionObject} providing array.
	 */
	public static final String OBJECT_NAME = DependencyKeys.ARRAY.name();

	/**
	 * Name of the {@link FunctionFlow} for rendering.
	 */
	public static final String FLOW_NAME = FlowKeys.RENDER_ELEMENT.name();

	/*
	 * ====================== ManagedFunctionSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_COMPONENT_TYPE_NAME, "Component Type");
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Obtain the component type name
		String componentTypeName = context.getProperty("component.type.name");
		Class<?> componentType = context.loadClass(componentTypeName);
		Class<?> arrayType = Array.newInstance(componentType, 0).getClass();

		// Create the function
		HttpTemplateArrayIteratorFunction function = new HttpTemplateArrayIteratorFunction();

		// Specify the function
		ManagedFunctionTypeBuilder<DependencyKeys, FlowKeys> functionBuilder = namespaceTypeBuilder
				.addManagedFunctionType(FUNCTION_NAME, function, DependencyKeys.class, FlowKeys.class);
		functionBuilder.addObject(arrayType).setKey(DependencyKeys.ARRAY);
		ManagedFunctionFlowTypeBuilder<FlowKeys> flow = functionBuilder.addFlow();
		flow.setKey(FlowKeys.RENDER_ELEMENT);
		flow.setArgumentType(componentType);
	}

	/**
	 * {@link HttpTemplateArrayIteratorManagedFunctionSource} dependency keys.
	 */
	public static enum DependencyKeys {
		ARRAY
	}

	/**
	 * {@link HttpTemplateArrayIteratorManagedFunctionSource} flow keys.
	 */
	public static enum FlowKeys {
		RENDER_ELEMENT
	}

	/**
	 * {@link ManagedFunction} implementation.
	 */
	public static class HttpTemplateArrayIteratorFunction extends StaticManagedFunction<DependencyKeys, FlowKeys> {

		/*
		 * ======================== ManagedFunction ========================
		 */

		@Override
		public Object execute(ManagedFunctionContext<DependencyKeys, FlowKeys> context) {

			// Obtain the array
			Object[] array = (Object[]) context.getObject(DependencyKeys.ARRAY);
			if (array == null) {
				return null; // no array, no rendering
			}

			// Iterate over the array rendering the elements
			for (Object element : array) {
				context.doFlow(FlowKeys.RENDER_ELEMENT, element, null);
			}

			// No argument for next task
			return null;
		}
	}

}