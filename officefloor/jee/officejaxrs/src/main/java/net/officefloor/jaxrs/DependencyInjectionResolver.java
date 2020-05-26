/*-
 * #%L
 * JAX-RS
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

package net.officefloor.jaxrs;

import org.glassfish.jersey.internal.inject.Injectee;
import org.glassfish.jersey.internal.inject.InjectionResolver;

import net.officefloor.plugin.managedobject.clazz.Dependency;

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
