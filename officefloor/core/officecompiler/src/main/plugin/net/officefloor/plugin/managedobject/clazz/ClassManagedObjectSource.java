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

package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Method;
import java.util.List;

import net.officefloor.compile.ManagedObjectSourceService;
import net.officefloor.compile.ManagedObjectSourceServiceFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencies;
import net.officefloor.plugin.clazz.dependency.ClassDependenciesContext;
import net.officefloor.plugin.clazz.dependency.ClassDependenciesManager;
import net.officefloor.plugin.clazz.dependency.ClassItemIndex;
import net.officefloor.plugin.clazz.factory.ClassObjectFactory;
import net.officefloor.plugin.clazz.factory.ClassObjectManufacturer;

/**
 * {@link ManagedObjectSource} that manages an {@link Object} via reflection.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassManagedObjectSource extends AbstractManagedObjectSource<Indexed, Indexed> implements
		ManagedObjectSourceService<Indexed, Indexed, ClassManagedObjectSource>, ManagedObjectSourceServiceFactory {

	/**
	 * Property name providing the {@link Class} name.
	 */
	public static final String CLASS_NAME_PROPERTY_NAME = "class.name";

	/**
	 * {@link ClassDependencies}.
	 */
	private ClassDependenciesManager dependencies;

	/**
	 * {@link ClassObjectFactory}.
	 */
	private ClassObjectFactory objectFactory;

	/*
	 * =================== ManagedObjectSourceService ==========================
	 */

	@Override
	public ManagedObjectSourceService<?, ?, ?> createService(ServiceContext context) throws Throwable {
		return this;
	}

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

	/**
	 * Loads the {@link Method}.
	 */
	@FunctionalInterface
	private static interface MethodsLoader {
		void loadMethods(List<Method> methods) throws Exception;
	}

	@Override
	protected void loadMetaData(MetaDataContext<Indexed, Indexed> context) throws Exception {
		ManagedObjectSourceContext<Indexed> mosContext = context.getManagedObjectSourceContext();

		// Obtain the class
		String className = mosContext.getProperty(CLASS_NAME_PROPERTY_NAME);
		final Class<?> objectClass = mosContext.loadClass(className);

		// Provide managed object class to indicate coordinating
		context.setManagedObjectClass(ClassManagedObject.class);

		// Class is the object type returned from the managed object
		context.setObjectClass(objectClass);

		// Create the dependencies
		this.dependencies = ClassDependenciesManager.create(objectClass, mosContext, new ClassDependenciesContext() {

			@Override
			public ClassItemIndex addFlow(String flowName, Class<?> argumentType, Object[] annotations) {
				// Add flow to managed object
				Labeller<Indexed> flowLabeller = context.addFlow(argumentType);
				flowLabeller.setLabel(flowName);
				return ClassDependenciesManager.createClassItemIndex(flowLabeller.getIndex(), null);
			}

			@Override
			public ClassItemIndex addDependency(String dependencyName, String qualifier, Class<?> objectType,
					Object[] annotations) {
				// Add dependency to managed object
				DependencyLabeller<Indexed> dependencyLabeller = context.addDependency(objectType);
				dependencyLabeller.setLabel(dependencyName);
				if (qualifier != null) {
					dependencyLabeller.setTypeQualifier(qualifier);
				}
				for (Object annotation : annotations) {
					dependencyLabeller.addAnnotation(annotation);
				}
				return ClassDependenciesManager.createClassItemIndex(dependencyLabeller.getIndex(),
						(annotation) -> dependencyLabeller.addAnnotation(annotation));
			}

			@Override
			public void addEscalation(Class<? extends Throwable> escalationType) {
				// No escalation for object
			}

			@Override
			public void addAnnotation(Object annotation) {
				// No annotations for object
			}
		});

		// Create the object factory
		ClassObjectManufacturer manufacturer = new ClassObjectManufacturer(dependencies, mosContext);
		this.objectFactory = manufacturer.constructClassObjectFactory(objectClass);

		// Add the object class as extension interface
		ClassExtensionFactory.registerExtension(context, objectClass);
	}

	@Override
	public void start(ManagedObjectExecuteContext<Indexed> context) throws Exception {

		// Load the execute context to dependency factories
		this.dependencies.loadManagedObjectExecuteContext(context);

		// Clear to allow garbage collection
		this.dependencies = null;
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new ClassManagedObject(this.objectFactory);
	}

}
