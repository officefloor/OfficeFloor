/*-
 * #%L
 * OfficeCompiler
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
