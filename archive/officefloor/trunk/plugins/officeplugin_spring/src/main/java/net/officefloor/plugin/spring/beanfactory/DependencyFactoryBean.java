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
package net.officefloor.plugin.spring.beanfactory;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.Indexed;
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
 *     &lt;property name=&quot;type&quot; value=&quot;[fully qualified type name]&quot; /&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * @author Daniel Sagenschneider
 *
 * @see BeanFactoryManagedObjectSource
 */
public class DependencyFactoryBean implements FactoryBean {

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
	}

	/**
	 * Type of the dependency.
	 */
	private Class<?> type;

	/**
	 * Index of the dependency from the {@link ObjectRegistry}.
	 */
	private int dependencyIndex = -1;

	/**
	 * The type expected from the {@link FactoryBean} must be specified in
	 * Spring configuration. This enables providing type for the
	 * {@link ManagedObjectDependencyType}.
	 *
	 * @param type
	 *            Type of object that this {@link FactoryBean} is to return.
	 */
	@Required
	public void setType(Class<?> type) {
		this.type = type;
	}

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

	/*
	 * ================== FactoryBean ================================
	 */

	@Override
	public boolean isSingleton() {
		return false;
	}

	@Override
	public Object getObject() throws Exception {

		// Obtain the dependency
		DependencyState state = threadLocalObjectRegistry.get();
		Object object = state.objectRegistry.getObject(this.dependencyIndex);

		// Return the dependency
		return object;
	}

	@Override
	public Class<?> getObjectType() {
		return this.type;
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
	}

}