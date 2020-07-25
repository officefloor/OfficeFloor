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

package net.officefloor.plugin.administration.clazz;

import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.GovernanceManager;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;

/**
 * {@link ClassDependencyFactory} to obtain the {@link GovernanceManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationGovernanceParameterFactory implements ClassDependencyFactory {

	/**
	 * Index of the {@link GovernanceManager}.
	 */
	private final int governanceIndex;

	/**
	 * Initiate.
	 * 
	 * @param governanceIndex Index of the {@link GovernanceManager}.
	 */
	public AdministrationGovernanceParameterFactory(int governanceIndex) {
		this.governanceIndex = governanceIndex;
	}

	/*
	 * ==================== ParameterFactory ========================
	 */

	@Override
	public Object createDependency(ManagedObject managedObject, ManagedObjectContext context,
			ObjectRegistry<Indexed> registry) throws Throwable {
		throw new IllegalStateException(GovernanceManager.class.getSimpleName() + " not available in "
				+ ManagedObjectContext.class.getSimpleName());
	}

	@Override
	public Object createDependency(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {
		throw new IllegalStateException(GovernanceManager.class.getSimpleName() + " not available in "
				+ ManagedFunctionContext.class.getSimpleName());
	}

	@Override
	public Object createDependency(AdministrationContext<Object, Indexed, Indexed> context) throws Throwable {
		return context.getGovernance(this.governanceIndex);
	}

}
