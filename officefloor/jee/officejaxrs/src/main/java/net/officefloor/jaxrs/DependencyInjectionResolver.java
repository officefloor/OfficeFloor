/*-
 * #%L
 * JAX-RS
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

package net.officefloor.jaxrs;

import org.glassfish.jersey.internal.inject.Injectee;
import org.glassfish.jersey.internal.inject.InjectionResolver;

import net.officefloor.plugin.clazz.Dependency;

/**
 * {@link Dependency} {@link InjectionResolver}.
 * 
 * @author Daniel Sagenschneider
 */
public class DependencyInjectionResolver implements InjectionResolver<Dependency> {

	/**
	 * {@link OfficeFloorDependencies}.
	 */
	private final OfficeFloorDependencies dependencies;

	/**
	 * Instantiate.
	 * 
	 * @param dependencies {@link OfficeFloorDependencies}.
	 */
	public DependencyInjectionResolver(OfficeFloorDependencies dependencies) {
		this.dependencies = dependencies;
	}

	/*
	 * ===================== InjectionResolver =====================
	 */

	@Override
	public Class<Dependency> getAnnotation() {
		return Dependency.class;
	}

	@Override
	public boolean isConstructorParameterIndicator() {
		return false;
	}

	@Override
	public boolean isMethodParameterIndicator() {
		return false;
	}

	@Override
	public Object resolve(Injectee injectee) {
		return this.dependencies.getDependency(injectee.getParent());
	}

}
