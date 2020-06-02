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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;

/**
 * {@link CoordinatingManagedObject} to dependency inject the {@link Object}
 * instance and make it available for use.
 * 
 * @author Daniel Sagenschneider
 * 
 */
public class ClassManagedObject implements ContextAwareManagedObject, CoordinatingManagedObject<Indexed> {

	/**
	 * {@link Constructor} to instantiate the object.
	 */
	private final Constructor<?> constructor;

	/**
	 * {@link Constructor} {@link ClassDependencyFactory} instances.
	 */
	private final ClassDependencyFactory[] constructorDependencyFactories;

	/**
	 * {@link ClassDependencyInjector} instances.
	 */
	private final ClassDependencyInjector[] dependencyInjectors;

	/**
	 * {@link ManagedObjectContext}.
	 */
	private ManagedObjectContext context;

	/**
	 * {@link Object} being managed by reflection.
	 */
	private Object object;

	/**
	 * Instantiate.
	 * 
	 * @param constructor                    {@link Constructor} to instantiate the
	 *                                       object.
	 * @param constructorDependencyFactories {@link ClassDependencyFactory}
	 *                                       instances for parameters of the
	 *                                       {@link Constructor}.
	 * @param dependencyInjectors            {@link ClassDependencyInjector}
	 *                                       instances to load remaining
	 *                                       dependencies.
	 */
	public ClassManagedObject(Constructor<?> constructor, ClassDependencyFactory[] constructorDependencyFactories,
			ClassDependencyInjector[] dependencyInjectors) {
		this.constructor = constructor;
		this.constructorDependencyFactories = constructorDependencyFactories;
		this.dependencyInjectors = dependencyInjectors;
	}

	/*
	 * ================= ContextAwareManagedObject ====================
	 */

	@Override
	public void setManagedObjectContext(ManagedObjectContext context) {
		this.context = context;
	}

	/*
	 * ================= CoordinatingManagedObject ====================
	 */

	@Override
	public void loadObjects(ObjectRegistry<Indexed> registry) throws Throwable {

		// Create the constructor parameters
		Object[] constructorParameters = new Object[this.constructorDependencyFactories.length];
		for (int i = 0; i < this.constructorDependencyFactories.length; i++) {
			ClassDependencyFactory factory = this.constructorDependencyFactories[i];
			constructorParameters[i] = factory.createDependency(this, this.context, registry);
		}

		// Construct an instance of the object
		Object object;
		try {
			object = this.constructor.newInstance(constructorParameters);
		} catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}

		// Load the dependencies
		for (int i = 0; i < this.dependencyInjectors.length; i++) {
			this.dependencyInjectors[i].injectDependencies(object, this, this.context, registry);
		}
	}

	@Override
	public Object getObject() {
		return this.object;
	}

}