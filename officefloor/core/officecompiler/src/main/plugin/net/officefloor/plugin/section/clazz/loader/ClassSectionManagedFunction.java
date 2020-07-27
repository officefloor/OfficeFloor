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

package net.officefloor.plugin.section.clazz.loader;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SectionFunction;

/**
 * {@link SectionFunction} with meta-data for {@link ClassSectionLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionManagedFunction extends ClassSectionFlow {

	/**
	 * {@link ManagedFunctionType}.
	 */
	private final ManagedFunctionType<?, ?> managedFunctionType;

	/**
	 * Instantiate.
	 * 
	 * @param function            {@link SectionFunction}.
	 * @param argumentType        Argument type for the {@link SectionFlowSinkNode}.
	 *                            May be <code>null</code> for no argument.
	 * @param managedFunctionType {@link ManagedFunctionType}.
	 */
	public ClassSectionManagedFunction(SectionFunction function, ManagedFunctionType<?, ?> managedFunctionType,
			Class<?> argumentType) {
		super(function, argumentType);
		this.managedFunctionType = managedFunctionType;
	}

	/**
	 * Obtains the {@link SectionFunction}.
	 * 
	 * @return {@link SectionFunction}.
	 */
	public SectionFunction getFunction() {
		return this.getFlowSink();
	}

	/**
	 * Obtains the {@link ManagedFunctionType}.
	 * 
	 * @return {@link ManagedFunctionType}.
	 */
	public ManagedFunctionType<?, ?> getManagedFunctionType() {
		return this.managedFunctionType;
	}

	/*
	 * ==================== ClassSectionFlow ======================
	 */

	@Override
	public SectionFunction getFlowSink() {
		return (SectionFunction) super.getFlowSink();
	}

}
