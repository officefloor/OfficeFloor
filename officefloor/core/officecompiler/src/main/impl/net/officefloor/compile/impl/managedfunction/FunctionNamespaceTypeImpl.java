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

package net.officefloor.compile.impl.managedfunction;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;

/**
 * {@link FunctionNamespaceType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionNamespaceTypeImpl implements FunctionNamespaceType, FunctionNamespaceBuilder {

	/**
	 * Listing of the {@link ManagedFunctionType} definitions.
	 */
	private final List<ManagedFunctionType<?, ?>> functions = new LinkedList<ManagedFunctionType<?, ?>>();

	/*
	 * =================== FunctionNamespaceBuilder ===================
	 */

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <M extends Enum<M>, F extends Enum<F>> ManagedFunctionTypeBuilder<M, F> addManagedFunctionType(
			String taskName, Class<M> objectKeysClass, Class<F> flowKeysClass) {
		ManagedFunctionTypeImpl functionType = new ManagedFunctionTypeImpl(taskName, objectKeysClass, flowKeysClass);
		this.functions.add(functionType);
		return functionType;
	}

	/*
	 * =================== FunctionNamespaceType ===================
	 */

	@Override
	public ManagedFunctionType<?, ?>[] getManagedFunctionTypes() {
		return this.functions.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getFunctionName(), b.getFunctionName()))
				.toArray(ManagedFunctionType[]::new);
	}

}
