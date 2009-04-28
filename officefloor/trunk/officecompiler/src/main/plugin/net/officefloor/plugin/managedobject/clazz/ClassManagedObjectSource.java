/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.ManagedObjectSourceService;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} that manages an {@link Object} via reflection.
 * 
 * @author Daniel
 */
public class ClassManagedObjectSource extends
		AbstractManagedObjectSource<Indexed, None> implements
		ManagedObjectSourceService {

	/**
	 * Convenience method to aid in unit testing.
	 * 
	 * @param clazz
	 *            {@link Class} to instantiate and have dependencies injected.
	 * @param dependencyNameObjectListing
	 *            Listing of dependency name and dependency object pairs to be
	 *            injected.
	 * @return Instance of the {@link Class} with the dependencies injected.
	 * @throws Exception
	 *             If fails to instantiate the instance and inject the
	 *             dependencies.
	 */
	public static <T> T newInstance(Class<T> clazz,
			Object... dependencyNameObjectListing) throws Exception {

		// Create the map of dependencies
		Map<String, Object> dependencies = new HashMap<String, Object>();
		for (int i = 0; i < dependencyNameObjectListing.length; i += 2) {
			String name = dependencyNameObjectListing[i].toString();
			Object dependency = dependencyNameObjectListing[i + 1];
			dependencies.put(name, dependency);
		}

		// Return the new instance
		return newInstance(clazz, dependencies);
	}

	/**
	 * <p>
	 * Convenience method to aid in unit testing.
	 * <p>
	 * As many {@link Dependency} {@link Field} instances will be
	 * <code>private</code> they are unlikely to be accessible in unit tests
	 * unless a specific constructor is provided. This method enables
	 * instantiation and injecting of dependencies to enable unit testing.
	 * 
	 * @param clazz
	 *            {@link Class} to instantiate and have dependencies injected.
	 * @param dependencies
	 *            Map of dependencies by the dependency name. The dependency
	 *            name is the {@link Dependency} {@link Field} name. Should two
	 *            {@link Field} instances in the class hierarchy have the same
	 *            name, the dependency name is qualified with the declaring
	 *            {@link Class} name.
	 * @return Instance of the {@link Class} with the dependencies injected.
	 * @throws Exception
	 *             If fails to instantiate the instance and inject the
	 *             dependencies.
	 */
	public static <T> T newInstance(Class<T> clazz,
			Map<String, Object> dependencies) throws Exception {

		// Instantiate the object
		T object = clazz.newInstance();

		// Obtain the listing of dependency fields
		List<Field> dependencyFields = retrieveOrderedDependencyFields(clazz);

		// Inject the dependencies
		for (Field dependencyField : dependencyFields) {

			// Obtain the dependency name
			String dependencyName = retrieveDependencyName(dependencyField,
					dependencyFields);

			// Obtain the dependency
			Object dependency = dependencies.get(dependencyName);

			// Inject the dependency
			dependencyField.setAccessible(true);
			dependencyField.set(object, dependency);
		}

		// Return the instance with dependencies injected
		return object;
	}

	/**
	 * Property name providing the {@link Class} name.
	 */
	public static final String CLASS_NAME_PROPERTY_NAME = "class.name";

	/**
	 * {@link Class} of the {@link Object} being managed.
	 */
	private Class<?> objectClass;

	/**
	 * {@link DependencyMetaData} instances.
	 */
	private DependencyMetaData[] dependencyMetaData;

	/*
	 * =================== ManagedObjectSourceService ==========================
	 */

	@Override
	public String getManagedObjectSourceAlias() {
		return "CLASS";
	}

	@Override
	public Class<? extends ManagedObjectSource<?, ?>> getManagedObjectSourceClass() {
		return this.getClass();
	}

	/*
	 * ==================== AbstractManagedObjectSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CLASS_NAME_PROPERTY_NAME, "Class");
	}

	@Override
	protected void loadMetaData(MetaDataContext<Indexed, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the class
		String className = mosContext.getProperty(CLASS_NAME_PROPERTY_NAME);
		this.objectClass = mosContext.getClassLoader().loadClass(className);

		// Provide managed object class to indicate coordinating
		context.setManagedObjectClass(ClassManagedObject.class);

		// Class is the object type returned from the managed object
		context.setObjectClass(this.objectClass);

		// Obtains the dependency fields
		List<Field> dependencyFields = retrieveOrderedDependencyFields(this.objectClass);

		// Create the dependency meta-data and register the dependencies
		List<DependencyMetaData> dependencyListing = new LinkedList<DependencyMetaData>();
		int dependencyIndex = 0;
		for (Field dependencyField : dependencyFields) {

			// Obtain the name for the dependency
			String dependencyName = retrieveDependencyName(dependencyField,
					dependencyFields);

			// Obtain the type for the dependency
			Class<?> dependencyType = dependencyField.getType();

			// Register the dependency
			context.addDependency(dependencyType).setLabel(dependencyName);

			// Add the dependency meta-data, ensuring can access field
			dependencyField.setAccessible(true);
			dependencyListing.add(new DependencyMetaData(dependencyIndex++,
					dependencyField));
		}
		this.dependencyMetaData = dependencyListing
				.toArray(new DependencyMetaData[0]);

		// Add the object class as extension interface.
		ClassExtensionInterfaceFactory.registerExtensionInterface(context,
				this.objectClass);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {

		// Create an instance of the object
		Object object = this.objectClass.newInstance();

		// Return a managed object to manage the object
		return new ClassManagedObject(object);
	}

	/**
	 * {@link ExtensionInterfaceFactory} that return the object of the
	 * {@link ClassManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	private static class ClassExtensionInterfaceFactory implements
			ExtensionInterfaceFactory {

		/**
		 * Registers the extension interface.
		 * 
		 * @param context
		 *            {@link MetaDataContext} to add the extension interface.
		 * @param objectClass
		 *            Object class which is the extension interface. This allows
		 *            any implemented interfaces to be extension interfaces for
		 *            this managed object.
		 */
		public static void registerExtensionInterface(
				MetaDataContext<Indexed, None> context, Class<?> objectClass) {
			context.addManagedObjectExtensionInterface(objectClass,
					new ClassExtensionInterfaceFactory());
		}

		/*
		 * ================ ExtensionInterfaceFactory =======================
		 */

		@Override
		public Object createExtensionInterface(ManagedObject managedObject) {

			// Downcast to the class managed object
			ClassManagedObject classManagedObject = (ClassManagedObject) managedObject;

			// Return the object as the extension interface
			return classManagedObject.getObject();
		}
	}

	/**
	 * {@link CoordinatingManagedObject} for dependency injecting the
	 * {@link Object}.
	 */
	private class ClassManagedObject implements
			CoordinatingManagedObject<Indexed> {

		/**
		 * {@link Object} being managed by reflection.
		 */
		private final Object object;

		/**
		 * Initiate.
		 * 
		 * @param object
		 *            {@link Object} being managed by reflection.
		 */
		public ClassManagedObject(Object object) {
			this.object = object;
		}

		/*
		 * ================= CoordinatingManagedObject ====================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Indexed> registry)
				throws Throwable {

			// Inject the dependencies
			for (int i = 0; i < ClassManagedObjectSource.this.dependencyMetaData.length; i++) {
				DependencyMetaData metaData = ClassManagedObjectSource.this.dependencyMetaData[i];

				// Obtain the dependency
				Object dependency = registry.getObject(metaData.index);

				// Inject the dependency
				metaData.field.set(this.object, dependency);
			}
		}

		@Override
		public Object getObject() {
			return this.object;
		}
	}

	/**
	 * Meta-data for a {@link Dependency}.
	 */
	private class DependencyMetaData {

		/**
		 * Index of the dependency within the {@link ObjectRegistry}.
		 */
		public final int index;

		/**
		 * {@link Field} to receive the injected dependency.
		 */
		public final Field field;

		/**
		 * Initiate.
		 * 
		 * @param index
		 *            Index of the dependency within the {@link ObjectRegistry}.
		 * @param field
		 *            {@link Field} to receive the injected dependency.
		 */
		public DependencyMetaData(int index, Field field) {
			this.index = index;
			this.field = field;
		}
	}

	/**
	 * Retrieves the dependency name for {@link Dependency} {@link Field}.
	 * 
	 * @param dependencyField
	 *            {@link Dependency} {@link Field}.
	 * @param dependencyFields
	 *            Listing of all {@link Dependency} {@link Field} instances.
	 * @return Dependency name for the {@link Dependency} {@link Field}.
	 */
	private static String retrieveDependencyName(Field dependencyField,
			List<Field> dependencyFields) {

		// Determine the name for the dependency
		String dependencyName;
		String fieldDependencyName = dependencyField.getName();
		boolean isAnotherByFieldName = false;
		for (Field field : dependencyFields) {
			if (field != dependencyField) {
				if (field.getName().equals(fieldDependencyName)) {
					// Another field by the same name
					isAnotherByFieldName = true;
				}
			}
		}
		if (!isAnotherByFieldName) {
			// Field name unique so use it
			dependencyName = fieldDependencyName;
		} else {
			// Field name not unique, so add class name for making unique
			String classDependencyName = dependencyField.getDeclaringClass()
					.getSimpleName()
					+ "." + dependencyField.getName();
			boolean isAnotherByClassName = false;
			for (Field field : dependencyFields) {
				if (field != dependencyField) {
					String fieldClassName = field.getDeclaringClass()
							.getSimpleName()
							+ "." + field.getName();
					if (fieldClassName.equals(classDependencyName)) {
						// Another field by same class name
						isAnotherByClassName = true;
					}
				}
			}
			if (!isAnotherByClassName) {
				// Class name unique so use it
				dependencyName = classDependencyName;
			} else {
				// Use fully qualified name of field
				dependencyName = dependencyField.getDeclaringClass().getName()
						+ "." + dependencyField.getName();
			}
		}

		// Return the dependency name
		return dependencyName;
	}

	/**
	 * Retrieves the {@link Dependency} {@link Field} instances ordered by their
	 * names.
	 * 
	 * @param clazz
	 *            {@link Class} to interrogate for {@link Dependency}
	 *            {@link Field} instances.
	 * @return Listing of {@link Dependency} {@link Field} instances ordered by
	 *         their names.
	 */
	private static List<Field> retrieveOrderedDependencyFields(Class<?> clazz) {

		// Create the listing of dependency fields (excluding Object fields)
		List<Field> dependencyFields = new LinkedList<Field>();
		Class<?> interrogateClass = clazz;
		while ((interrogateClass != null)
				&& (!Object.class.equals(interrogateClass))) {
			for (Field field : interrogateClass.getDeclaredFields()) {
				if (field.getAnnotation(Dependency.class) != null) {
					// Annotated as a dependency field
					dependencyFields.add(field);
				}
			}
			interrogateClass = interrogateClass.getSuperclass();
		}

		// Sort the dependency fields by field name, then class, then package.
		// This is necessary to keep indexes of dependencies the same.
		Collections.sort(dependencyFields, new Comparator<Field>() {
			@Override
			public int compare(Field a, Field b) {
				// Compare by field names first
				if (!(a.getName().equals(b.getName()))) {
					// Field names different so compare by them
					return a.getName().compareTo(b.getName());
				}

				// Same field name so use the simple class name
				Class<?> aClass = a.getDeclaringClass();
				Class<?> bClass = b.getDeclaringClass();
				if (!(aClass.getSimpleName().equals(bClass.getSimpleName()))) {
					// Simple class names different so compare by them
					return aClass.getSimpleName().compareTo(
							bClass.getSimpleName());
				}

				// Field and simple class name same so compare by package
				return aClass.getPackage().getName().compareTo(
						bClass.getPackage().getName());
			}
		});

		// Return the dependency fields
		return dependencyFields;
	}

}