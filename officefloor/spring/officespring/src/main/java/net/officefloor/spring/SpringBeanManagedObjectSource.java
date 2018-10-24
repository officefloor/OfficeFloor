/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.spring;

import org.springframework.context.ConfigurableApplicationContext;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * Spring bean {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringBeanManagedObjectSource extends AbstractManagedObjectSource<Indexed, None> implements ManagedObject {

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
	 * Instantiate.
	 * 
	 * @param beanName      Name of the Spring bean.
	 * @param objectType    Object type expected from Spring.
	 * @param springContext Spring {@link ConfigurableApplicationContext}.
	 */
	public SpringBeanManagedObjectSource(String beanName, Class<?> objectType,
			ConfigurableApplicationContext springContext) {
		this.beanName = beanName;
		this.objectType = objectType;
		this.springContext = springContext;
	}

	/*
	 * ===================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<Indexed, None> context) throws Exception {
		context.setObjectClass(this.objectType);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	@Override
	public Object getObject() throws Throwable {
		return this.springContext.getBean(this.beanName);
	}

}