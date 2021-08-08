/*-
 * #%L
 * Procedure
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

package net.officefloor.activity.procedure.build;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.officefloor.activity.impl.procedure.ProcedureLoaderCompilerRunnable;
import net.officefloor.activity.impl.procedure.ProcedureLoaderImpl;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.section.ProcedureManagedFunctionSource;
import net.officefloor.activity.procedure.section.ProcedureSectionSource;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSourceContext;

/**
 * Employs {@link ProcedureArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureEmployer {

	/**
	 * Names of the {@link Object} {@link Method} instances.
	 */
	private static Set<String> objectMethodNames = new HashSet<>();

	/**
	 * Initiate the {@link Object} {@link Method} names.
	 */
	static {
		for (Method method : Object.class.getMethods()) {
			objectMethodNames.add(method.getName());
		}
	}

	/**
	 * <p>
	 * Convenience method to list {@link Method} instances from a {@link Class}.
	 * <p>
	 * This handles not including {@link Object} methods.
	 * 
	 * @param clazz   {@link Class} to extract {@link Procedure} names.
	 * @param exclude {@link Predicate} to filter out {@link Method} instances. May
	 *                be <code>null</code> to include all.
	 * @param handler Handler for the candidate {@link Method}.
	 */
	public static void listMethods(Class<?> clazz, Predicate<Method> exclude, Consumer<Method> handler) {

		// Load the procedure names
		NEXT_METHOD: for (Method method : clazz.getMethods()) {
			String methodName = method.getName();

			// Ignore if object method
			if (objectMethodNames.contains(methodName)) {
				continue NEXT_METHOD;
			}

			// Ignore if exclude
			if ((exclude != null) && (exclude.test(method))) {
				continue NEXT_METHOD;
			}

			// Add the method
			handler.accept(method);
		}
	}

	/**
	 * Creates the {@link ProcedureLoader}.
	 * 
	 * @param compiler {@link OfficeFloorCompiler}.
	 * @return {@link ProcedureLoader}.
	 * @throws Exception If fails to create {@link ProcedureLoader}.
	 */
	public static ProcedureLoader employProcedureLoader(OfficeFloorCompiler compiler) throws Exception {
		return compiler.run(ProcedureLoaderCompilerRunnable.class);
	}

	/**
	 * Creates the {@link ProcedureLoader}.
	 * 
	 * @param architect {@link OfficeArchitect}.
	 * @param context   {@link OfficeSourceContext}.
	 * @return {@link ProcedureLoader}.
	 * @throws Exception If fails to create {@link ProcedureLoader}.
	 */
	public static ProcedureLoader employProcedureLoader(OfficeArchitect architect, OfficeSourceContext context)
			throws Exception {
		return new ProcedureLoaderImpl(architect, context);
	}

	/**
	 * Creates the {@link ProcedureLoader}.
	 * 
	 * @param designer {@link SectionDesigner}.
	 * @param context  {@link SectionSourceContext}.
	 * @return {@link ProcedureLoader}.
	 * @throws Exception If fails to create {@link ProcedureLoader}.
	 */
	public static ProcedureLoader employProcedureLoader(SectionDesigner designer, SectionSourceContext context)
			throws Exception {
		return new ProcedureLoaderImpl(designer, context);
	}

	/**
	 * Employs the {@link ProcedureArchitect}.
	 * 
	 * @param officeArchitect     {@link OfficeArchitect}.
	 * @param officeSourceContext {@link OfficeSourceContext}.
	 * @return {@link ProcedureArchitect}.
	 */
	public static ProcedureArchitect<OfficeSection> employProcedureArchitect(OfficeArchitect officeArchitect,
			OfficeSourceContext officeSourceContext) {
		return new ProcedureArchitect<OfficeSection>() {

			@Override
			public OfficeSection addProcedure(String sectionName, String className, String serviceName,
					String procedureName, boolean isNext, PropertyList properties) {
				OfficeSection procedure = officeArchitect.addOfficeSection(sectionName,
						ProcedureSectionSource.class.getName(), procedureName);
				if (properties != null) {
					properties.configureProperties(procedure);
				}
				procedure.addProperty(ProcedureManagedFunctionSource.RESOURCE_PROPERTY_NAME, className);
				procedure.addProperty(ProcedureManagedFunctionSource.SOURCE_NAME_PROPERTY_NAME, serviceName);
				if (isNext) {
					procedure.addProperty(ProcedureSectionSource.IS_NEXT_PROPERTY_NAME, Boolean.TRUE.toString());
				}
				return procedure;
			}
		};
	}

	/**
	 * Employs the {@link ProcedureArchitect}.
	 * 
	 * @param sectionDesigner      {@link SectionDesigner}.
	 * @param sectionSourceContext {@link SectionSourceContext}.
	 * @return {@link ProcedureArchitect}.
	 */
	public static ProcedureArchitect<SubSection> employProcedureDesigner(SectionDesigner sectionDesigner,
			SectionSourceContext sectionSourceContext) {
		return new ProcedureArchitect<SubSection>() {

			@Override
			public SubSection addProcedure(String sectionName, String resource, String serviceName,
					String procedureName, boolean isNext, PropertyList properties) {
				SubSection procedure = sectionDesigner.addSubSection(sectionName,
						ProcedureSectionSource.class.getName(), procedureName);
				if (properties != null) {
					properties.configureProperties(procedure);
				}
				procedure.addProperty(ProcedureManagedFunctionSource.RESOURCE_PROPERTY_NAME, resource);
				procedure.addProperty(ProcedureManagedFunctionSource.SOURCE_NAME_PROPERTY_NAME, serviceName);
				if (isNext) {
					procedure.addProperty(ProcedureSectionSource.IS_NEXT_PROPERTY_NAME, Boolean.TRUE.toString());
				}
				return procedure;
			}
		};
	}

}
