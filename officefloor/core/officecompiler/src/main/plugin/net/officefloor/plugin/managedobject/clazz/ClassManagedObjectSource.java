/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
import java.util.logging.Logger;

import net.officefloor.compile.ManagedObjectSourceService;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.managedobject.clazz.DependencyMetaData.DependencyType;

/**
 * {@link ManagedObjectSource} that manages an {@link Object} via reflection.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassManagedObjectSource extends AbstractManagedObjectSource<Indexed, Indexed>
		implements ManagedObjectSourceService<Indexed, Indexed, ClassManagedObjectSource> {

	/**
	 * Convenience method to aid in unit testing.
	 * 
	 * @param <T>                         {@link Class} type.
	 * @param clazz                       {@link Class} to instantiate and have
	 *                                    dependencies injected.
	 * @param dependencyNameObjectListing Listing of dependency name and dependency
	 *                                    object pairs to be injected.
	 * @return Instance of the {@link Class} with the dependencies injected.
	 * @throws Exception If fails to instantiate the instance and inject the
	 *                   dependencies.
	 */
	public static <T> T newInstance(Class<T> clazz, Object... dependencyNameObjectListing) throws Exception {

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
	 * <code>private</code> they are unlikely to be accessible in unit tests unless
	 * a specific constructor is provided. This method enables instantiation and
	 * injecting of dependencies to enable unit testing.
	 * 
	 * @param <T>          {@link Class} type.
	 * @param clazz        {@link Class} to instantiate and have dependencies
	 *                     injected.
	 * @param dependencies Map of dependencies by the dependency name. The
	 *                     dependency name is the {@link Dependency} {@link Field}
	 *                     name. Should two {@link Field} instances in the class
	 *                     hierarchy have the same name, the dependency name is
	 *                     qualified with the declaring {@link Class} name.
	 * @return Instance of the {@link Class} with the dependencies injected.
	 * @throws Exception If fails to instantiate the instance and inject the
	 *                   dependencies.
	 */
	public static <T> T newInstance(Class<T> clazz, Map<String, Object> dependencies) throws Exception {

		// Instantiate the object
		T object = clazz.getDeclaredConstructor().newInstance();

		// Obtain the listing of dependency fields
		List<Field> dependencyFields = retrieveDependencyFields(clazz);
		orderFields(dependencyFields);

		// Inject the dependencies
		for (Field dependencyField : dependencyFields) {

			// Obtain the dependency name
			String dependencyName = retrieveDependencyName(dependencyField, dependencyFields);

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
			String dependencyName = retrieveDependencyName(processField, processFields);

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
	 * Default {@link Constructor} arguments.
	 */
	private static final Object[] DEFAULT_CONSTRUCTOR_ARGUMENTS = new Object[0];

	/**
	 * {@link Constructor} for the {@link Object} being managed.
	 */
	private Constructor<?> objectConstructor;

	/**
	 * {@link DependencyMetaData} instances.
	 */
	private DependencyMetaData[] dependencyMetaData;

	/**
	 * {@link ManagedObjectSourceContext}.
	 */
	private ManagedObjectSourceContext<Indexed> mosContext;

	/**
	 * {@link ProcessStruct} instances.
	 */
	private List<ProcessStruct> processStructs;

	/**
	 * {@link ProcessMetaData} instances.
	 */
	private ProcessMetaData[] processMetaData;

	/**
	 * Allows overriding the extraction of the dependency {@link Field} instances.
	 * 
	 * @param objectClass Class to extract dependency {@link Field} instances.
	 * @return Listing of {@link Field} instances to be dependency injected.
	 */
	protected List<Field> extractDependencyFields(Class<?> objectClass) {
		return retrieveDependencyFields(objectClass);
	}

	/**
	 * Extracts the {@link DependencyMetaData} from the object class.
	 * 
	 * @param objectClass Object class to interrogate for the
	 *                    {@link DependencyMetaData} instances.
	 * @return {@link DependencyMetaData} instances.
	 */
	public DependencyMetaData[] extractDependencyMetaData(Class<?> objectClass) {

		// Obtains the ordered dependency fields
		List<Field> dependencyFields = this.extractDependencyFields(objectClass);
		orderFields(dependencyFields);

		// Create the dependency meta-data and register the dependencies
		List<DependencyMetaData> dependencyListing = new LinkedList<DependencyMetaData>();
		int dependencyIndex = 0;
		for (Field dependencyField : dependencyFields) {

			// Ensure field is accessible (for injection)
			dependencyField.setAccessible(true);

			// Obtain the required type for the field
			Class<?> requiredType = dependencyField.getType();

			// Determine if requiring managed object context
			if (requiredType.isAssignableFrom(ManagedObjectContext.class)) {
				// Inject the Managed Object Context
				dependencyListing.add(new DependencyMetaData(DependencyType.MANAGE_OBJECT_CONTEXT, dependencyField));

			} else if (requiredType.isAssignableFrom(Logger.class)) {
				// Inject Logger from Managed Object Context
				dependencyListing.add(new DependencyMetaData(DependencyType.LOGGER, dependencyField));

			} else {
				// Inject dependent object
				String dependencyName = retrieveDependencyName(dependencyField, dependencyFields);
				dependencyListing.add(new DependencyMetaData(dependencyName, dependencyIndex++, dependencyField));
			}
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
	protected void loadMetaData(MetaDataContext<Indexed, Indexed> context) throws Exception {
		ManagedObjectSourceContext<Indexed> mosContext = context.getManagedObjectSourceContext();

		// Obtain the class
		String className = mosContext.getProperty(CLASS_NAME_PROPERTY_NAME);
		Class<?> objectClass = mosContext.getClassLoader().loadClass(className);

		// Obtain the default constructor
		this.objectConstructor = objectClass.getConstructor(new Class<?>[0]);

		// Provide managed object class to indicate coordinating
		context.setManagedObjectClass(ClassManagedObject.class);

		// Class is the object type returned from the managed object
		context.setObjectClass(objectClass);

		// Create the dependency meta-data and register the dependencies
		this.dependencyMetaData = this.extractDependencyMetaData(objectClass);
		for (DependencyMetaData dependency : this.dependencyMetaData) {

			// Only register dependencies
			if (DependencyType.DEPENDENCY.equals(dependency.type)) {

				// Register the dependency
				DependencyLabeller<Indexed> labeller = context.addDependency(dependency.field.getType());

				// Use field name as name of dependency
				labeller.setLabel(dependency.name);

				// Determine type qualifier
				String typeQualifier = dependency.getTypeQualifier();
				if (!CompileUtil.isBlank(typeQualifier)) {
					// Specify the type qualifier
					labeller.setTypeQualifier(typeQualifier);
				}
			}
		}

		// Obtain the process details
		this.processStructs = retrieveOrderedProcessStructs(objectClass);

		// Register the processes to be invoked
		for (ProcessStruct processStruct : this.processStructs) {

			// Ensure can access field
			processStruct.field.setAccessible(true);

			// Register the process methods for the field
			for (ProcessMethodStruct processMethod : processStruct.invokeMethods) {

				// Obtain the process name
				String processName = retrieveProcessName(processStruct.field, processMethod.method,
						this.processStructs);

				// Obtain the argument type
				Class<?> argumentType = null;
				if (processMethod.isParameter) {
					Class<?>[] methodParameters = processMethod.method.getParameterTypes();
					argumentType = methodParameters[0];
				}

				// Add the flow
				context.addFlow(argumentType).setLabel(processName);
			}
		}

		// Hold reference to class loader for start method
		this.mosContext = mosContext;

		// Add the object class as extension interface.
		ClassExtensionFactory.registerExtension(context, objectClass);
	}

	@Override
	public void start(ManagedObjectExecuteContext<Indexed> context) throws Exception {

		// Create the process meta-data
		List<ProcessMetaData> processListing = new LinkedList<ProcessMetaData>();
		int processIndex = 0;
		for (ProcessStruct struct : this.processStructs) {

			// Create the map of method name to its process index
			Map<String, ProcessMethodMetaData> methodMetaData = new HashMap<>(struct.invokeMethods.length);
			for (ProcessMethodStruct methodStruct : struct.invokeMethods) {
				methodMetaData.put(methodStruct.method.getName(), new ProcessMethodMetaData(processIndex++,
						methodStruct.isParameter, methodStruct.isFlowCallback));
			}

			// Create and add the process meta-data
			processListing.add(new ProcessMetaData(struct.field, methodMetaData, this.mosContext, context));
		}

		// Only required process meta-data
		this.processMetaData = processListing.toArray(new ProcessMetaData[0]);
		this.mosContext = null; // discard as no longer required
		this.processStructs = null; // discard to allow garbage collection
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {

		// Create an instance of the object
		Object object;
		try {
			object = this.objectConstructor.newInstance(DEFAULT_CONSTRUCTOR_ARGUMENTS);
		} catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}

		// Return a managed object to manage the object
		return new ClassManagedObject(object, this.dependencyMetaData, this.processMetaData);
	}

	/**
	 * Retrieves the unique {@link Dependency} inject name for {@link Field}.
	 * 
	 * @param field           {@link Field}.
	 * @param allInjectFields Listing of all {@link Dependency} inject {@link Field}
	 *                        instances.
	 * @return Unique {@link Dependency} inject name for the {@link Field}.
	 */
	private static String retrieveDependencyName(Field injectField, List<Field> allInjectFields) {

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
			String classInjectName = injectField.getDeclaringClass().getSimpleName() + "." + injectField.getName();
			boolean isAnotherByClassName = false;
			for (Field field : allInjectFields) {
				if (field != injectField) {
					String fieldClassName = field.getDeclaringClass().getSimpleName() + "." + field.getName();
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
				injectName = injectField.getDeclaringClass().getName() + "." + injectField.getName();
			}
		}

		// Return the inject name
		return injectName;
	}

	/**
	 * Retrieves the {@link Dependency} {@link Field} instances.
	 * 
	 * @param clazz {@link Class} to interrogate for {@link Dependency}
	 *              {@link Field} instances.
	 * @return Listing of {@link Dependency} {@link Field} instances ordered by
	 *         their names.
	 */
	private static List<Field> retrieveDependencyFields(Class<?> clazz) {

		// Create the listing of dependency fields (excluding Object fields)
		List<Field> dependencyFields = new LinkedList<Field>();
		Class<?> interrogateClass = clazz;
		while ((interrogateClass != null) && (!Object.class.equals(interrogateClass))) {
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
	 * @param processInterfaceField {@link Field} annotated with
	 *                              {@link ProcessInterface}.
	 * @param processMethod         {@link Method} of the {@link Field} type.
	 * @param processStructs        Details of all {@link ProcessInterface}
	 *                              annotated {@link Field} instances.
	 * @return Unique process name.
	 */
	private static String retrieveProcessName(Field processInterfaceField, Method processMethod,
			List<ProcessStruct> processStructs) {

		// Determine the process name
		String processName;
		String methodName = processMethod.getName();
		boolean isAnotherByMethodName = false;
		for (ProcessStruct struct : processStructs) {
			for (ProcessMethodStruct invokeMethodStruct : struct.invokeMethods) {
				Method invokeMethod = invokeMethodStruct.method;
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
			String fieldProcessName = processInterfaceField.getName() + "." + processMethod.getName();
			boolean isAnotherByFieldName = false;
			for (ProcessStruct struct : processStructs) {
				for (ProcessMethodStruct invokeMethodStruct : struct.invokeMethods) {
					Method invokeMethod = invokeMethodStruct.method;
					if (processMethod != invokeMethod) {
						String compareName = struct.field.getName() + "." + invokeMethod.getName();
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
				processName = processInterfaceField.getDeclaringClass().getName() + "."
						+ processInterfaceField.getName() + "." + processMethod.getName();
			}
		}

		// Return the process name
		return processName;
	}

	/**
	 * <p>
	 * Retrieves the {@link ProcessStruct} instances for the {@link FlowInterface}
	 * {@link Field} instances ordered by:
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
	 * @param clazz {@link Class} to interrogate for {@link FlowInterface}
	 *              {@link Field} instances.
	 * @return Listing of {@link ProcessInterface} {@link Field} instances ordered
	 *         by their names.
	 * @throws Exception Should a {@link ProcessInterface} injection type be
	 *                   invalid.
	 */
	private static List<ProcessStruct> retrieveOrderedProcessStructs(Class<?> clazz) throws Exception {

		// Create the listing of process fields (excluding Object fields)
		List<Field> processFields = new LinkedList<Field>();
		Class<?> interrogateClass = clazz;
		while ((interrogateClass != null) && (!Object.class.equals(interrogateClass))) {
			for (Field field : interrogateClass.getDeclaredFields()) {
				if (field.getType().getAnnotation(FlowInterface.class) != null) {
					// Annotated as a flow interface field
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
				throw new Exception("Type for field " + processField.getName()
						+ " must be an interface as the type is annotated with " + FlowInterface.class.getSimpleName()
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
					throw new Exception("May not have duplicate process method names (field=" + processField.getName()
							+ ", type=" + type.getName() + ", method=" + methodName + ")");
				}
				methodNames.add(methodName);

				// Ensure at most only one parameter to the method
				Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes.length > 1) {
					throw new Exception("Process methods may only have at most one parameter (field="
							+ processField.getName() + ", method=" + methodName + ")");

				}

				// Ensure no return from method
				Class<?> returnType = method.getReturnType();
				if ((returnType != null) && (!(Void.TYPE.equals(returnType)))) {
					throw new Exception("Process methods may only be void in return type (field="
							+ processField.getName() + ", method=" + methodName + ")");
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

			// Create the process method structs
			ProcessMethodStruct[] processMethodStructs = new ProcessMethodStruct[invokeMethods.length];
			for (int i = 0; i < processMethodStructs.length; i++) {
				Method method = invokeMethods[i];

				// Determine if parameter and flow callback
				boolean isParameter = false;
				boolean isFlowCallback = false;
				Class<?>[] methodParams = method.getParameterTypes();
				switch (methodParams.length) {
				case 2:
					// Two parameters, first parameter, second flow callback
					isParameter = true;
					if (!FlowCallback.class.isAssignableFrom(methodParams[1])) {
						throw new Exception("Second parameter must be " + FlowCallback.class.getSimpleName()
								+ " (field=" + processField.getName() + ", method=" + method.getName() + ")");
					}
					isFlowCallback = true;
					break;

				case 1:
					// Single parameter, either parameter or flow callback
					if (FlowCallback.class.isAssignableFrom(methodParams[0])) {
						isFlowCallback = true;
					} else {
						isParameter = true;
					}
					break;

				case 0:
					// No parameters
					break;

				default:
					// Invalid to have more than two parameter
					throw new Exception(
							"Flow methods may only have at most two parameters [<parameter>, <flow callback>] (field="
									+ processField.getName() + ", method=" + method.getName() + ")");
				}

				// Load the process method struct
				processMethodStructs[i] = new ProcessMethodStruct(method, isParameter, isFlowCallback);
			}

			// Create and add the process interface details
			processStructs.add(new ProcessStruct(processField, processMethodStructs));
		}

		// Return the process fields
		return processStructs;
	}

	/**
	 * Details of a {@link FlowInterface} on the class.
	 */
	private static class ProcessStruct {

		/**
		 * {@link Field} to inject the {@link FlowInterface}.
		 */
		public final Field field;

		/**
		 * {@link Method} instances to invoke the processes.
		 */
		public final ProcessMethodStruct[] invokeMethods;

		/**
		 * Initiate.
		 * 
		 * @param field         {@link Field} to inject the {@link ProcessInterface}.
		 * @param invokeMethods {@link Method} instances to invoke the processes.
		 */
		public ProcessStruct(Field field, ProcessMethodStruct[] invokeMethods) {
			this.field = field;
			this.invokeMethods = invokeMethods;
		}
	}

	/**
	 * Details of the {@link Method} of a {@link FlowInterface}.
	 */
	private static class ProcessMethodStruct {

		/**
		 * {@link Method} to invoke the process.
		 */
		private final Method method;

		/**
		 * Indicates if parameter in invoking the process.
		 */
		private boolean isParameter;

		/**
		 * Indicates if {@link FlowCallback} in invoking the process.
		 */
		private boolean isFlowCallback;

		/**
		 * Instantiate.
		 * 
		 * @param method         {@link Method} to invoke the process.
		 * @param isParameter    Indicates if parameter in invoking the process.
		 * @param isFlowCallback Indicates if {@link FlowCallback} in invoking the
		 *                       process.
		 */
		public ProcessMethodStruct(Method method, boolean isParameter, boolean isFlowCallback) {
			this.method = method;
			this.isParameter = isParameter;
			this.isFlowCallback = isFlowCallback;
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
	 * @param fields {@link Field} instances to order.
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
					return aClass.getSimpleName().compareTo(bClass.getSimpleName());
				}

				// Field and simple class name same so compare by package
				return aClass.getPackage().getName().compareTo(bClass.getPackage().getName());
			}
		});
	}

}