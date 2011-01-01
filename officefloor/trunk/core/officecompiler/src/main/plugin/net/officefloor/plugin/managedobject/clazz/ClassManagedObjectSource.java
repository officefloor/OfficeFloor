/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.ManagedObjectSourceService;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} that manages an {@link Object} via reflection.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassManagedObjectSource extends
		AbstractManagedObjectSource<Indexed, Indexed> implements
		ManagedObjectSourceService<Indexed, Indexed, ClassManagedObjectSource> {

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
		List<Field> dependencyFields = retrieveDependencyFields(clazz);
		orderFields(dependencyFields);

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

		// Obtain the listing of process fields
		List<ProcessStruct> processStructs = retrieveOrderedProcessStructs(clazz);
		List<Field> processFields = new ArrayList<Field>(processStructs.size());
		for (ProcessStruct processStruct : processStructs) {
			processFields.add(processStruct.field);
		}

		// Inject the processes
		for (Field processField : processFields) {

			// Obtain the process name (as dependency inject interface)
			String dependencyName = retrieveDependencyName(processField,
					processFields);

			// Obtain the dependency (process interface)
			Object dependency = dependencies.get(dependencyName);

			// Inject the process interface
			processField.setAccessible(true);
			processField.set(object, dependency);
		}

		// Return the instance with dependencies and process interfaces injected
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

	/**
	 * {@link ClassLoader}.
	 */
	private ClassLoader classLoader;

	/**
	 * {@link ProcessStruct} instances.
	 */
	private List<ProcessStruct> processStructs;

	/**
	 * {@link ProcessMetaData} instances.
	 */
	private ProcessMetaData[] processMetaData;

	/**
	 * Allows overriding the extraction of the dependency {@link Field}
	 * instances.
	 * 
	 * @param objectClass
	 *            Class to extract dependency {@link Field} instances.
	 * @return Listing of {@link Field} instances to be dependency injected.
	 */
	protected List<Field> extractDependencyFields(Class<?> objectClass) {
		return retrieveDependencyFields(objectClass);
	}

	/**
	 * Extracts the {@link DependencyMetaData} from the object class.
	 * 
	 * @param objectClass
	 *            Object class to interrogate for the {@link DependencyMetaData}
	 *            instances.
	 * @return {@link DependencyMetaData} instances.
	 */
	public DependencyMetaData[] extractDependencyMetaData(Class<?> objectClass) {

		// Obtains the ordered dependency fields
		List<Field> dependencyFields = this
				.extractDependencyFields(objectClass);
		orderFields(dependencyFields);

		// Create the dependency meta-data and register the dependencies
		List<DependencyMetaData> dependencyListing = new LinkedList<DependencyMetaData>();
		int dependencyIndex = 0;
		for (Field dependencyField : dependencyFields) {

			// Obtain the name for the dependency
			String dependencyName = retrieveDependencyName(dependencyField,
					dependencyFields);

			// Obtain the type for the dependency
			Class<?> dependencyType = dependencyField.getType();

			// Add the dependency meta-data, ensuring can access field
			dependencyField.setAccessible(true);
			dependencyListing.add(new DependencyMetaData(dependencyName,
					dependencyIndex++, dependencyField));
		}
		DependencyMetaData[] dependencyMetaData = dependencyListing
				.toArray(new DependencyMetaData[dependencyListing.size()]);

		// Return the dependency meta-data
		return dependencyMetaData;
	}

	/*
	 * =================== ManagedObjectSourceService ==========================
	 */

	@Override
	public String getManagedObjectSourceAlias() {
		return "CLASS";
	}

	@Override
	public Class<ClassManagedObjectSource> getManagedObjectSourceClass() {
		return ClassManagedObjectSource.class;
	}

	/*
	 * ==================== AbstractManagedObjectSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CLASS_NAME_PROPERTY_NAME, "Class");
	}

	@Override
	protected void loadMetaData(MetaDataContext<Indexed, Indexed> context)
			throws Exception {
		ManagedObjectSourceContext<Indexed> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the class
		String className = mosContext.getProperty(CLASS_NAME_PROPERTY_NAME);
		this.objectClass = mosContext.getClassLoader().loadClass(className);

		// Provide managed object class to indicate coordinating
		context.setManagedObjectClass(ClassManagedObject.class);

		// Class is the object type returned from the managed object
		context.setObjectClass(this.objectClass);

		// Create the dependency meta-data and register the dependencies
		this.dependencyMetaData = this
				.extractDependencyMetaData(this.objectClass);
		for (DependencyMetaData dependency : this.dependencyMetaData) {
			// Register the dependency
			context.addDependency(dependency.field.getType()).setLabel(
					dependency.name);
		}

		// Obtain the process details
		this.processStructs = retrieveOrderedProcessStructs(this.objectClass);

		// Register the processes to be invoked
		for (ProcessStruct processStruct : this.processStructs) {

			// Ensure can access field
			processStruct.field.setAccessible(true);

			// Register the process methods for the field
			for (Method processMethod : processStruct.invokeMethods) {

				// Obtain the process name
				String processName = retrieveProcessName(processStruct.field,
						processMethod, this.processStructs);

				// Obtain the argument type
				Class<?>[] methodParameters = processMethod.getParameterTypes();
				Class<?> argumentType = (methodParameters.length == 1 ? methodParameters[0]
						: null);

				// Add the flow
				context.addFlow(argumentType).setLabel(processName);
			}
		}

		// Hold reference to class loader for start method
		this.classLoader = mosContext.getClassLoader();

		// Add the object class as extension interface.
		ClassExtensionInterfaceFactory.registerExtensionInterface(context,
				this.objectClass);
	}

	@Override
	public void start(ManagedObjectExecuteContext<Indexed> context)
			throws Exception {

		// Create the process meta-data
		List<ProcessMetaData> processListing = new LinkedList<ProcessMetaData>();
		int processIndex = 0;
		for (ProcessStruct struct : this.processStructs) {

			// Create the map of method name to its process index
			Map<String, Integer> indexes = new HashMap<String, Integer>(
					struct.invokeMethods.length);
			for (Method invokeMethod : struct.invokeMethods) {
				indexes
						.put(invokeMethod.getName(),
								new Integer(processIndex++));
			}

			// Create and add the process meta-data
			processListing.add(new ProcessMetaData(struct.field, indexes,
					this.classLoader, context));
		}

		// Only required process meta-data
		this.processMetaData = processListing.toArray(new ProcessMetaData[0]);
		this.classLoader = null; // discard as no longer required
		this.processStructs = null; // discard to allow garbage collection
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {

		// Create an instance of the object
		Object object = this.objectClass.newInstance();

		// Return a managed object to manage the object
		return new ClassManagedObject(object, this.dependencyMetaData,
				this.processMetaData);
	}

	/**
	 * Retrieves the unique {@link Dependency} inject name for {@link Field}.
	 * 
	 * @param field
	 *            {@link Field}.
	 * @param allInjectFields
	 *            Listing of all {@link Dependency} inject {@link Field}
	 *            instances.
	 * @return Unique {@link Dependency} inject name for the {@link Field}.
	 */
	private static String retrieveDependencyName(Field injectField,
			List<Field> allInjectFields) {

		// Determine the name for the inject field
		String injectName;
		String fieldInjectName = injectField.getName();
		boolean isAnotherByFieldName = false;
		for (Field field : allInjectFields) {
			if (field != injectField) {
				if (field.getName().equals(fieldInjectName)) {
					// Another field by the same name
					isAnotherByFieldName = true;
				}
			}
		}
		if (!isAnotherByFieldName) {
			// Field name unique so use it
			injectName = fieldInjectName;
		} else {
			// Field name not unique, so add class name for making unique
			String classInjectName = injectField.getDeclaringClass()
					.getSimpleName()
					+ "." + injectField.getName();
			boolean isAnotherByClassName = false;
			for (Field field : allInjectFields) {
				if (field != injectField) {
					String fieldClassName = field.getDeclaringClass()
							.getSimpleName()
							+ "." + field.getName();
					if (fieldClassName.equals(classInjectName)) {
						// Another field by same class name
						isAnotherByClassName = true;
					}
				}
			}
			if (!isAnotherByClassName) {
				// Class name unique so use it
				injectName = classInjectName;
			} else {
				// Use fully qualified name of field
				injectName = injectField.getDeclaringClass().getName() + "."
						+ injectField.getName();
			}
		}

		// Return the inject name
		return injectName;
	}

	/**
	 * Retrieves the {@link Dependency} {@link Field} instances.
	 * 
	 * @param clazz
	 *            {@link Class} to interrogate for {@link Dependency}
	 *            {@link Field} instances.
	 * @return Listing of {@link Dependency} {@link Field} instances ordered by
	 *         their names.
	 */
	private static List<Field> retrieveDependencyFields(Class<?> clazz) {

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

		// Return the dependency fields
		return dependencyFields;
	}

	/**
	 * Obtains the unique process name.
	 * 
	 * @param processInterfaceField
	 *            {@link Field} annotated with {@link ProcessInterface}.
	 * @param processMethod
	 *            {@link Method} of the {@link Field} type.
	 * @param processStructs
	 *            Details of all {@link ProcessInterface} annotated
	 *            {@link Field} instances.
	 * @return Unique process name.
	 */
	private static String retrieveProcessName(Field processInterfaceField,
			Method processMethod, List<ProcessStruct> processStructs) {

		// Determine the process name
		String processName;
		String methodName = processMethod.getName();
		boolean isAnotherByMethodName = false;
		for (ProcessStruct struct : processStructs) {
			for (Method invokeMethod : struct.invokeMethods) {
				if (processMethod != invokeMethod) {
					if (invokeMethod.getName().equals(methodName)) {
						// Another method by the same name
						isAnotherByMethodName = true;
					}
				}
			}
		}
		if (!isAnotherByMethodName) {
			// Method name unique so use it
			processName = methodName;
		} else {
			// Method name not unique, so add field name for making unique
			String fieldProcessName = processInterfaceField.getName() + "."
					+ processMethod.getName();
			boolean isAnotherByFieldName = false;
			for (ProcessStruct struct : processStructs) {
				for (Method invokeMethod : struct.invokeMethods) {
					if (processMethod != invokeMethod) {
						String compareName = struct.field.getName() + "."
								+ invokeMethod.getName();
						if (compareName.equals(fieldProcessName)) {
							// Another method by field name
							isAnotherByFieldName = true;
						}
					}
				}
			}
			if (!isAnotherByFieldName) {
				// Field name unique so use it
				processName = fieldProcessName;
			} else {
				// Use fully qualified name of process method
				processName = processInterfaceField.getDeclaringClass()
						.getName()
						+ "."
						+ processInterfaceField.getName()
						+ "."
						+ processMethod.getName();
			}
		}

		// Return the process name
		return processName;
	}

	/**
	 * <p>
	 * Retrieves the {@link ProcessStruct} instances for the
	 * {@link ProcessInterface} {@link Field} instances ordered by:
	 * <ol>
	 * <li>field name</li>
	 * <li>simple class name . field name</li>
	 * <li>fully qualified class . field name</li>
	 * </ol>
	 * with all {@link Method} instances of each {@link Field} sorted by the
	 * {@link Method} name.
	 * <p>
	 * Ordering is necessary to ensure similar indexes each time loaded.
	 * 
	 * @param clazz
	 *            {@link Class} to interrogate for {@link ProcessInterface}
	 *            {@link Field} instances.
	 * @return Listing of {@link ProcessInterface} {@link Field} instances
	 *         ordered by their names.
	 * @throws Exception
	 *             Should a {@link ProcessInterface} injection type be invalid.
	 */
	private static List<ProcessStruct> retrieveOrderedProcessStructs(
			Class<?> clazz) throws Exception {

		// Create the listing of process fields (excluding Object fields)
		List<Field> processFields = new LinkedList<Field>();
		Class<?> interrogateClass = clazz;
		while ((interrogateClass != null)
				&& (!Object.class.equals(interrogateClass))) {
			for (Field field : interrogateClass.getDeclaredFields()) {
				if (field.getAnnotation(ProcessInterface.class) != null) {
					// Annotated as a process field
					processFields.add(field);
				}
			}
			interrogateClass = interrogateClass.getSuperclass();
		}

		// Order the fields
		orderFields(processFields);

		// Create the listing of process structs
		List<ProcessStruct> processStructs = new LinkedList<ProcessStruct>();
		for (Field processField : processFields) {

			// Obtain the process field type
			Class<?> type = processField.getType();
			if (!type.isInterface()) {
				throw new Exception(
						"Type for field "
								+ processField.getName()
								+ " must be an interface as the fields is annotated with "
								+ ProcessInterface.class.getSimpleName()
								+ " (type=" + type.getName() + ")");
			}

			// Obtain the non Object methods
			List<Method> methods = new LinkedList<Method>();
			Set<String> methodNames = new HashSet<String>();
			for (Method method : type.getMethods()) {

				// Ensure not an Object method
				if (Object.class.equals(method.getDeclaringClass())) {
					continue;
				}

				// Ensure the method name is unique for the type
				String methodName = method.getName();
				if (methodNames.contains(methodName)) {
					throw new Exception(
							"May not have duplicate process method names (field="
									+ processField.getName() + ", type="
									+ type.getName() + ", method=" + methodName
									+ ")");
				}
				methodNames.add(methodName);

				// Ensure at most only one parameter to the method
				Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes.length > 1) {
					throw new Exception(
							"Process methods may only have at most one parameter (field="
									+ processField.getName() + ", method="
									+ methodName + ")");

				}

				// Ensure no return from method
				Class<?> returnType = method.getReturnType();
				if ((returnType != null) && (!(Void.TYPE.equals(returnType)))) {
					throw new Exception(
							"Process methods may only be void in return type (field="
									+ processField.getName() + ", method="
									+ methodName + ")");
				}

				// Add the valid method
				methods.add(method);
			}

			// Create the sorted array of methods
			Method[] invokeMethods = methods.toArray(new Method[0]);
			Arrays.sort(invokeMethods, new Comparator<Method>() {
				@Override
				public int compare(Method a, Method b) {
					return a.getName().compareTo(b.getName());
				}
			});

			// Create and add the process interface details
			processStructs.add(new ProcessStruct(processField, invokeMethods));
		}

		// Return the process fields
		return processStructs;
	}

	/**
	 * Details of a {@link ProcessInterface} on the class.
	 */
	private static class ProcessStruct {

		/**
		 * {@link Field} to inject the {@link ProcessInterface}.
		 */
		public final Field field;

		/**
		 * {@link Method} instances to invoke the processes.
		 */
		public final Method[] invokeMethods;

		/**
		 * Initiate.
		 * 
		 * @param field
		 *            {@link Field} to inject the {@link ProcessInterface}.
		 * @param invokeMethods
		 *            {@link Method} instances to invoke the processes.
		 */
		public ProcessStruct(Field field, Method[] invokeMethods) {
			this.field = field;
			this.invokeMethods = invokeMethods;
		}
	}

	/**
	 * <p>
	 * Orders the {@link Field} instances.
	 * <p>
	 * {@link Dependency} {@link Field} instances are ordered by:
	 * <ol>
	 * <li>field name</li>
	 * <li>simple class name . field name</li>
	 * <li>fully qualified class . field name</li>
	 * </ol>
	 * <p>
	 * Ordering is necessary to ensure similar indexes each time loaded.
	 * 
	 * @param fields
	 *            {@link Field} instances to order.
	 */
	private static void orderFields(List<Field> fields) {

		// Sort the fields by field name, then class, then package.
		// This is necessary to keep indexes the same.
		Collections.sort(fields, new Comparator<Field>() {
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
	}

}