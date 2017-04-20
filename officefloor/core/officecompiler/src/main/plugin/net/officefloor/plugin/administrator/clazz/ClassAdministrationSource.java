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
package net.officefloor.plugin.administrator.clazz;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;

import net.officefloor.compile.AdministrationSourceService;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.administration.source.AdministrationSourceContext;
import net.officefloor.compile.spi.administration.source.impl.AbstractAdministratorSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.plugin.clazz.ClassFlowParameterFactory;
import net.officefloor.plugin.clazz.ClassFlowRegistry;
import net.officefloor.plugin.clazz.ClassFlowBuilder;
import net.officefloor.plugin.clazz.Sequence;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;

/**
 * {@link AdministrationSource} that delegates to {@link Object}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassAdministrationSource extends AbstractAdministratorSource<Object, Indexed, Indexed>
		implements AdministrationSourceService<Object, Indexed, Indexed, ClassAdministrationSource> {

	/**
	 * Property name providing the {@link Class} name.
	 */
	public static final String CLASS_NAME_PROPERTY_NAME = "class.name";

	/*
	 * =================== AdministrationSourceService ===================
	 */

	@Override
	public String getAdministrationSourceAlias() {
		return "CLASS";
	}

	@Override
	public Class<ClassAdministrationSource> getAdministrationSourceClass() {
		return ClassAdministrationSource.class;
	}

	/*
	 * =================== AbstractAdministrationSource =================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CLASS_NAME_PROPERTY_NAME, "Class");
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void loadMetaData(MetaDataContext<Object, Indexed, Indexed> context) throws Exception {
		AdministrationSourceContext adminContext = context.getAdministrationSourceContext();

		// Obtain the administrator class
		String adminClassName = adminContext.getProperty(CLASS_NAME_PROPERTY_NAME);
		Class<?> objectClass = adminContext.getClassLoader().loadClass(adminClassName);

		// Obtain the methods of class in sorted order (maintains indexes)
		Method[] methods = objectClass.getMethods();
		Arrays.sort(methods, new Comparator<Method>() {
			@Override
			public int compare(Method a, Method b) {
				return a.getName().compareTo(b.getName());
			}
		});

		// Interrogate for administration methods and extension interface
		Method adminMethod = null;
		Class extensionInterface = null;
		for (Method method : methods) {

			// Method must have only one parameter
			Class<?>[] paramTypes = method.getParameterTypes();
			if ((paramTypes == null) || (paramTypes.length < 1) || (paramTypes.length > 2)) {
				continue; // must have one or two parameters
			}

			// Obtain the extension types
			Class<?> paramType = paramTypes[0];
			if (!(paramType.isArray())) {
				continue; // must be an array
			}

			// Ensure the possible second parameter is flow interface
			ClassFlowParameterFactory flowParameterFactory = null;
			if (paramTypes.length == 2) {

				// Obtain flow interface details
				String functionName = method.getName();
				Class<?> parameterType = paramTypes[1];
				Sequence flowSequence = new Sequence();
				ClassFlowRegistry flowRegistry = (label, flowParameterType) -> {
					// Register the flow
					context.addFlow(flowParameterType).setLabel(label);
				};
				ClassLoader classLoader = context.getAdministrationSourceContext().getClassLoader();

				// Build the flow parameter factory
				flowParameterFactory = new ClassFlowBuilder(FlowInterface.class).buildFlowParameterFactory(functionName,
						parameterType, flowSequence, flowRegistry, classLoader);
				if (flowParameterFactory == null) {
					continue; // second parameter must be flow interface
				}
			}

			// Ensure only the one administration method
			if (adminMethod != null) {
				throw new Exception("Only one method on class " + objectClass.getName() + " should be administration ("
						+ method.getName() + ", " + adminMethod.getName() + ")");
			}

			// Use the method
			adminMethod = method;

			// Extension interface is component type for the array
			extensionInterface = paramType.getComponentType();
		}
		if (adminMethod == null) {
			throw new Exception("No administration method on class " + objectClass.getName());
		}

		// Provide the extension interface
		context.setExtensionInterface(extensionInterface);

		// Provide the administration factory
		boolean isStatic = Modifier.isStatic(adminMethod.getModifiers());
		Constructor<?> constructor = objectClass.getConstructor(new Class<?>[0]);
		context.setAdministrationFactory(new ClassAdministration((isStatic ? null : constructor), adminMethod));
	}

}