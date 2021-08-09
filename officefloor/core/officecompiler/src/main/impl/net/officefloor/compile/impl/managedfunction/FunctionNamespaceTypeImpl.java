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
