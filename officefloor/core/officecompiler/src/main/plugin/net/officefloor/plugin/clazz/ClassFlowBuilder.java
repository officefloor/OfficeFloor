/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.clazz;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.section.clazz.Spawn;

/**
 * Factory to create the {@link FlowInterface} {@link Flow} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassFlowBuilder<A extends Annotation> {

	/**
	 * {@link Class} of the {@link Annotation}.
	 */
	private final Class<A> annotationClass;

	/**
	 * Instantiate.
	 * 
	 * @param annotationClass {@link Class} of the {@link Annotation}.
	 */
	public ClassFlowBuilder(Class<A> annotationClass) {
		this.annotationClass = annotationClass;
	}

	/**
	 * Builds the {@link ClassFlowParameterFactory} for the {@link FlowInterface}
	 * parameter.
	 * 
	 * @param functionName  Name of {@link Method} containing the
	 *                      {@link FlowInterface} parameter.
	 * @param parameterType Interface {@link Class} for the {@link FlowInterface}.
	 * @param flowRegistry  {@link ClassFlowRegistry}.
	 * @param sourceContext {@link SourceContext}.
	 * @return {@link ClassFlowParameterFactory} or <code>null</code> if parameter
	 *         is not a {@link FlowInterface}.
	 * @throws Exception If fails to build the {@link ClassFlowParameterFactory}.
	 */
	public ClassFlowParameterFactory buildFlowParameterFactory(String functionName, Class<?> parameterType,
			ClassFlowRegistry flowRegistry, SourceContext sourceContext) throws Exception {

		// Determine if flow interface
		if (!parameterType.isAnnotationPresent(this.annotationClass)) {
			return null; // not a flow interface
		}

		// Ensure is an interface
		if (!parameterType.isInterface()) {
			throw new Exception("Parameter " + parameterType.getSimpleName() + " on method " + functionName
					+ " must be an interface as parameter type is annotated with "
					+ FlowInterface.class.getSimpleName());
		}

		// Obtain the methods sorted (deterministic order)
		Method[] flowMethods = parameterType.getMethods();
		Arrays.sort(flowMethods, new Comparator<Method>() {
			@Override
			public int compare(Method a, Method b) {
				return a.getName().compareTo(b.getName());
			}
		});

		// Create a flow for each method of the interface
		Map<String, ClassFlowMethodMetaData> flowMethodMetaDatas = new HashMap<String, ClassFlowMethodMetaData>(
				flowMethods.length);
		for (int m = 0; m < flowMethods.length; m++) {
			Method flowMethod = flowMethods[m];
			String flowMethodName = flowMethod.getName();

			// Not include object methods
			if (Object.class.equals(flowMethod.getDeclaringClass())) {
				continue;
			}

			// Ensure not duplicate flow names
			if (flowMethodMetaDatas.containsKey(flowMethodName)) {
				throw new Exception("May not have duplicate flow method names (function=" + functionName + ", flow="
						+ parameterType.getSimpleName() + "." + flowMethodName + ")");
			}

			// Ensure at appropriate parameters
			Class<?> flowParameterType = null;
			boolean isFlowCallback = false;
			Class<?>[] flowMethodParams = flowMethod.getParameterTypes();
			switch (flowMethodParams.length) {
			case 2:
				// Two parameters, first parameter, second flow callback
				flowParameterType = flowMethodParams[0];
				if (!FlowCallback.class.isAssignableFrom(flowMethodParams[1])) {
					throw new Exception("Second parameter must be " + FlowCallback.class.getSimpleName() + " (function "
							+ functionName + ", flow " + parameterType.getSimpleName() + "." + flowMethodName + ")");
				}
				isFlowCallback = true;
				break;

			case 1:
				// Single parameter, either parameter or flow callback
				if (FlowCallback.class.isAssignableFrom(flowMethodParams[0])) {
					isFlowCallback = true;
				} else {
					flowParameterType = flowMethodParams[0];
				}
				break;

			case 0:
				// No parameters
				break;

			default:
				// Invalid to have more than two parameter
				throw new Exception(
						"Flow methods may only have at most two parameters [<parameter>, <flow callback>] (function "
								+ functionName + ", flow " + parameterType.getSimpleName() + "." + flowMethodName
								+ ")");
			}

			// Ensure void return type
			Class<?> flowReturnType = flowMethod.getReturnType();
			if ((flowReturnType != null) && (!Void.TYPE.equals(flowReturnType))) {
				// Invalid return type
				throw new Exception("Flow method " + parameterType.getSimpleName() + "." + flowMethodName
						+ " return type is invalid (return type=" + flowReturnType.getName() + ", function="
						+ functionName + ").  Must not have return type.");
			}

			// Determine if spawn
			boolean isSpawn = flowMethod.isAnnotationPresent(Spawn.class);

			// Register the flow
			int flowIndex = flowRegistry.registerFlow(flowMethodName, flowParameterType);

			// Create and register the flow method meta-data
			ClassFlowMethodMetaData flowMethodMetaData = new ClassFlowMethodMetaData(parameterType, flowMethod, isSpawn,
					flowIndex, flowParameterType, isFlowCallback);
			flowMethodMetaDatas.put(flowMethodName, flowMethodMetaData);
		}

		// Create and return the flow interface parameter factory
		return new ClassFlowParameterFactory(sourceContext, parameterType, flowMethodMetaDatas);
	}

}
