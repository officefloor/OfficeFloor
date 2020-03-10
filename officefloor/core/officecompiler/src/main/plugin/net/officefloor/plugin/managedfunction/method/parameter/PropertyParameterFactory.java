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

package net.officefloor.plugin.managedfunction.method.parameter;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.plugin.clazz.Property;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;

/**
 * {@link MethodParameterFactory} for the {@link Property}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyParameterFactory implements MethodParameterFactory {

	/**
	 * {@link Property} value.
	 */
	private final String value;

	/**
	 * Instantiate.
	 * 
	 * @param value {@link Property} value.
	 */
	public PropertyParameterFactory(String value) {
		this.value = value;
	}

	/*
	 * ====================== ParameterFactory =============================
	 */

	@Override
	public Object createParameter(ManagedFunctionContext<?, ?> context) {
		return this.value;
	}

}
