/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.spring.beanfactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;

/**
 * <p>
 * Dependency {@link FactoryBean} to obtain the bean/object from the
 * {@link ObjectRegistry} (in other words an {@link OfficeFrame} configured
 * dependency).
 * <p>
 * Example configuration:
 * 
 * <pre>
 * &lt;bean id=&quot;[name]&quot; class=&quot;net.officefloor.plugin.spring.beanfactory.DependencyFactoryBean&quot; &gt;
 *     &lt;property name=&quot;type&quot; value=&quot;[fully qualified interface name]&quot; /&gt;
 * &lt;/bean&gt;
 * </pre>
 * <p>
 * Due to the necessity to create a {@link Proxy} to handle ordering of loading
 * {@link ManagedObject} instances, the <code>type</code> must be an interface.
 * 
 * @author Daniel Sagenschneider
 * 
 * @see BeanFactoryManagedObjectSource
 */
public class DependencyFactoryBean implements FactoryBean, InvocationHandler {

	/**
	 * Name of the Spring property to obtain the required type returned from
	 * this {@link DependencyFactoryBean}.
	 */
	public static final String TYPE_SPRING_PROPERTY = "type";

	/**
	 * Name of the Spring property to provide the dependency index.
	 */
	public static final String DEPENDENCY_INDEX_SPRING_PROPERTY = "dependencyIndex";

	/**
	 * Name of the Spring property to provide the {@link Proxy}
	 * {@link Constructor}.
	 */
	public static final String PROXY_CONSTRUCTOR_SPRING_PROPERTY = "proxyConstructor";

	/**
	 * {@link ThreadLocal} containing the {@link DependencyState} of this
	 * {@link DependencyFactoryBean} for the {@link Thread} using the
	 * {@link BeanFactory}.
	 */
	private static final ThreadLocal<DependencyState> threadLocalObjectRegistry = new ThreadLocal<DependencyState>() {
		@Override
		protected DependencyState initialValue() {
			return new DependencyState();
		}
	};

	/**
	 * <p>
	 * Specifies the {@link ObjectRegistry} to be used to source this dependency
	 * from the {@link OfficeFrame}.
	 * <p>
	 * Internally this uses a {@link ThreadLocal} so that calls to
	 * {@link BeanFactory#getBean(String)} use the correct
	 * {@link ObjectRegistry}. This somewhat provides a
	 * {@link ApplicationContext} in terms of &quot;context&quot;.
	 * 
	 * @param objectRegistry
	 *            {@link ObjectRegistry}.
	 */
	public static void setObjectRegistry(ObjectRegistry<Indexed> objectRegistry) {

		// Obtain the dependency state
		DependencyState state = threadLocalObjectRegistry.get();

		// Provide the object registry to state
		state.objectRegistry = objectRegistry;

		// Load dependencies for already instantiated factories
		for (DependencyFactoryBean factory : state.factories) {
			loadDependency(factory, objectRegistry);
		}

		// Clear list of factories as now setup (allows them to be gc'ed)
		state.factories.clear();
	}

	/**
	 * Loads the dependency for the {@link DependencyFactoryBean}.
	 * 
	 * @param factory
	 *            {@link DependencyFactoryBean}.
	 * @param objectRegistry
	 *            {@link ObjectRegistry}.
	 */
	private static void loadDependency(DependencyFactoryBean factory,
			ObjectRegistry<Indexed> objectRegistry) {
		factory.dependency = objectRegistry.getObject(factory.dependencyIndex);
	}

	/**
	 * <p>
	 * Requires the type expected from the {@link FactoryBean} to be specified
	 * in Spring configuration. This enables providing type for the
	 * {@link ManagedObjectDependencyType}.
	 * <p>
	 * Due to the necessity to create a {@link Proxy} to handle ordering of
	 * loading {@link ManagedObject} instances, the type must be an interface.
	 * 
	 * @param type
	 *            Type of object that this {@link FactoryBean} is to return.
	 */
	@Required
	public void setType(Class<?> type) {
		// Ignored as used by managed object source
	}

	/**
	 * Index of the dependency from the {@link ObjectRegistry}.
	 */
	private int dependencyIndex = -1;

	/**
	 * {@link Constructor} to create the {@link Proxy}.
	 */
	private Constructor<?> constructor;

	/**
	 * Dependency to delegate {@link Proxy} functionality.
	 */
	private Object dependency;

	/**
	 * <p>
	 * Specifies the index of the dependency from the {@link ObjectRegistry}.
	 * <p>
	 * This <b>must</b> not be configured into Spring configuration as it is
	 * added to Spring meta-data via the {@link BeanFactoryManagedObjectSource}.
	 * 
	 * @param dependencyIndex
	 *            Index of the dependency from the {@link ObjectRegistry}.
	 */
	public void setDependencyIndex(int dependencyIndex) {
		this.dependencyIndex = dependencyIndex;
	}

	/**
	 * <p>
	 * Specifies the {@link Constructor} to create the {@link Proxy}.
	 * <p>
	 * This <b>must</b> not be configured into Spring configuration as it is
	 * added to Spring meta-data via the {@link BeanFactoryManagedObjectSource}.
	 * 
	 * @param constructor
	 *            {@link Constructor}.
	 */
	public void setProxyConstructor(Constructor<?> constructor) {
		this.constructor = constructor;
	}

	/*
	 * ================== FactoryBean ================================
	 */

	@Override
	public boolean isSingleton() {
		return false;
	}

	@Override
	public Object getObject() throws Exception {

		// Obtain the dependency if object registry available
		DependencyState state = threadLocalObjectRegistry.get();
		if (state.objectRegistry != null) {
			// Obtain the dependency
			loadDependency(this, state.objectRegistry);

		} else {
			// Register this to receive object registry when available
			state.factories.add(this);
		}

		// Always return a proxy for consistency
		return this.constructor.newInstance(this);
	}

	@Override
	public Class<?> getObjectType() {
		return null;
	}

	/*
	 * =================== InvocationHandler ===========================
	 */

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {

		// Ensure have the dependency
		if (this.dependency == null) {
			throw new IllegalStateException(
					"Can not use functionality on a bean from "
							+ DependencyFactoryBean.class.getSimpleName()
							+ " before setup is complete");
		}

		// Obtain the method to invoke on the dependency
		Method dependencyMethod = this.dependency.getClass().getMethod(
				method.getName(), method.getParameterTypes());

		try {
			// Return the results of invoking the method on the dependency
			return dependencyMethod.invoke(this.dependency, args);

		} catch (InvocationTargetException ex) {
			// Propagate cause
			throw ex.getCause();
		}
	}

	/**
	 * State for this {@link DependencyFactoryBean} in context of the
	 * {@link Thread} using it.
	 */
	private static class DependencyState {

		/**
		 * {@link ObjectRegistry}.
		 */
		public ObjectRegistry<Indexed> objectRegistry = null;

		/**
		 * {@link DependencyFactoryBean} instances that were instantiated before
		 * the {@link ObjectRegistry} became available.
		 */
		public List<DependencyFactoryBean> factories = new LinkedList<DependencyFactoryBean>();
	}

}