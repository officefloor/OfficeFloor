/*-
 * #%L
 * Spring Integration
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

package net.officefloor.spring;

import org.springframework.context.ConfigurableApplicationContext;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for the {@link ConfigurableApplicationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ApplicationContextManagedObjectSource extends AbstractManagedObjectSource<None, None>
		implements ManagedObject {

	/**
	 * {@link ConfigurableApplicationContext}.
	 */
	private final ConfigurableApplicationContext applicationContext;

	/**
	 * Instantiate.
	 * 
	 * @param applicationContext {@link ConfigurableApplicationContext}.
	 */
	public ApplicationContextManagedObjectSource(ConfigurableApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/*
	 * ================== ManagedObjectSource ====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		context.setObjectClass(this.applicationContext.getClass());
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ===================== ManagedObject =======================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.applicationContext;
	}

}
