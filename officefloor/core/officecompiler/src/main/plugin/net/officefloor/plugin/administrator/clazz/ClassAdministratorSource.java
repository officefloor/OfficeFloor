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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.AdministrationSourceService;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.administration.source.AdministrationSourceContext;
import net.officefloor.compile.spi.administration.source.impl.AbstractAdministratorSource;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.Duty;
import net.officefloor.frame.api.administration.DutyKey;
import net.officefloor.frame.api.build.Indexed;

/**
 * {@link AdministrationSource} that delegates to {@link Object}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassAdministratorSource extends
		AbstractAdministratorSource<Object, Indexed> implements
		AdministrationSourceService<Object, Indexed, ClassAdministratorSource> {

	/**
	 * Property name providing the {@link Class} name.
	 */
	public static final String CLASS_NAME_PROPERTY_NAME = "class.name";

	/**
	 * {@link Class} of the {@link Object} providing administration methods.
	 */
	private Class<?> objectClass;

	/**
	 * Array type for the array of extension interfaces to pass to the
	 * administration {@link Method}.
	 */
	private Class<?> extensionInterfaceArrayType;

	/**
	 * {@link Duty} method instances in index order as per the {@link DutyKey}.
	 */
	private Method[] dutyMethods;

	/**
	 * Obtains the most specific extension interface type.
	 * 
	 * @param extensionInterface
	 *            Current extension interface type. May be <code>null</code> if
	 *            first administration {@link Method}.
	 * @param componentType
	 *            Type to determine if in hierarchy with extension interface.
	 * @return Most specific type.
	 * @throws Exception
	 *             If types are not assignable. Not able to obtain most specific
	 *             as either type is not a parent of the other type.
	 */
	private Class<?> getMostSpecificType(Class<?> extensionInterface,
			Class<?> componentType) throws Exception {

		// Ensure have extension interface
		if (extensionInterface == null) {
			return componentType;
		}

		// Determine which is assignable
		if (extensionInterface.isAssignableFrom(componentType)) {
			// Component type is more specific
			return componentType;
		} else if (componentType.isAssignableFrom(extensionInterface)) {
			// Extension interface contains to be more specific
			return extensionInterface;
		} else {
			// Either is not the parent of the other, no specific type
			throw new Exception("Incompatible extension interfaces ("
					+ extensionInterface.getClass().getName() + ", "
					+ componentType.getClass().getName() + ")");
		}
	}

	/*
	 * =================== AdministratorSourceService ==========================
	 */

	@Override
	public Class<ClassAdministratorSource> getAdministratorSourceClass() {
		return ClassAdministratorSource.class;
	}

	@Override
	public String getAdministratorSourceAlias() {
		return "CLASS";
	}

	/*
	 * =================== AbstractAdministratorSource =================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CLASS_NAME_PROPERTY_NAME, "Class");
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void loadMetaData(MetaDataContext<Object, Indexed> context)
			throws Exception {
		AdministrationSourceContext adminContext = context
				.getAdministratorSourceContext();

		// Obtain the administrator class
		String adminClassName = adminContext
				.getProperty(CLASS_NAME_PROPERTY_NAME);
		this.objectClass = adminContext.getClassLoader().loadClass(
				adminClassName);

		// Obtain the methods of class in sorted order (maintains indexes)
		Method[] methods = this.objectClass.getMethods();
		Arrays.sort(methods, new Comparator<Method>() {
			@Override
			public int compare(Method a, Method b) {
				return a.getName().compareTo(b.getName());
			}
		});

		// Interrogate for administration methods and extension interface
		Class extensionInterface = null;
		List<Method> adminMethods = new LinkedList<Method>();
		for (Method method : methods) {

			// Method must have only one parameter
			Class<?>[] paramTypes = method.getParameterTypes();
			if ((paramTypes == null) || (paramTypes.length != 1)) {
				continue; // must have one parameter
			}
			Class<?> paramType = paramTypes[0];

			// The parameter must be an array
			if (!(paramType.isArray())) {
				continue; // must be an array
			}

			// Obtain the component type for the array
			Class<?> componentType = paramType.getComponentType();

			// Keep track of the most specific extension interface type
			extensionInterface = this.getMostSpecificType(extensionInterface,
					componentType);

			// Add in the duty
			context.addDuty(method.getName());
			adminMethods.add(method);
		}

		// Provide the extension interface
		context.setExtensionInterface(extensionInterface);

		// Keep reference to create the administrators
		this.extensionInterfaceArrayType = extensionInterface;
		this.dutyMethods = adminMethods.toArray(new Method[0]);
	}

	@Override
	public Administration<Object, Indexed> createAdministrator()
			throws Exception {

		// Create the administration object
		Object object = this.objectClass.newInstance();

		// Return the administrator
		return new ClassAdministrator(object, this.extensionInterfaceArrayType,
				this.dutyMethods);
	}

}