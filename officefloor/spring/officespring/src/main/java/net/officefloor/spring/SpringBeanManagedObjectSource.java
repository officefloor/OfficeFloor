/*-
 * #%L
 * Spring Integration
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

package net.officefloor.spring;

import org.springframework.context.ConfigurableApplicationContext;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * Spring bean {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringBeanManagedObjectSource extends AbstractManagedObjectSource<Indexed, None>
		implements ManagedObject, CoordinatingManagedObject<Indexed> {

	/**
	 * Name of the Spring bean.
	 */
	private final String beanName;

	/**
	 * Object type expected from Spring.
	 */
	private final Class<?> objectType;

	/**
	 * Spring {@link ConfigurableApplicationContext}.
	 */
	private final ConfigurableApplicationContext springContext;

	/**
	 * {@link SpringDependency} instances.
	 */
	private final SpringDependency[] springDependencies;

	/**
	 * Instantiate.
	 * 
	 * @param beanName           Name of the Spring bean.
	 * @param objectType         Object type expected from Spring.
	 * @param springContext      Spring {@link ConfigurableApplicationContext}.
	 * @param springDependencies {@link SpringDependency} instances.
	 */
	public SpringBeanManagedObjectSource(String beanName, Class<?> objectType,
			ConfigurableApplicationContext springContext, SpringDependency[] springDependencies) {
		this.beanName = beanName;
		this.objectType = objectType;
		this.springContext = springContext;
		this.springDependencies = springDependencies;
	}

	/*
	 * ===================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no specification
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void loadMetaData(MetaDataContext<Indexed, None> context) throws Exception {

		// Configure meta-data
		context.setObjectClass(this.objectType);
		context.setManagedObjectClass(this.getClass());

		// Provide extension
		context.addManagedObjectExtension((Class) this.objectType, (mo) -> mo.getObject());

		// Load the dependencies
		for (SpringDependency springDependency : this.springDependencies) {
			String qualifier = springDependency.getQualifier();
			Class<?> type = springDependency.getObjectType();

			// Add the dependency
			DependencyLabeller<Indexed> dependency = context.addDependency(type);
			dependency.setTypeQualifier(qualifier);
			dependency.setLabel((qualifier == null ? "" : qualifier + ":") + type.getName());
		}
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ================= CoordinatingManagedObject ========================
	 */

	@Override
	public void loadObjects(ObjectRegistry<Indexed> registry) throws Throwable {
		// objects via supplier thread local
	}

	/*
	 * ======================== ManagedObject ==============================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.springContext.getBean(this.beanName);
	}

}
