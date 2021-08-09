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

package net.officefloor.plugin.section.clazz.loader;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Decoration of {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionDecoration {

	/**
	 * Indicates if include {@link ManagedFunctionType}.
	 * 
	 * @param functionType  {@link ManagedFunctionType}.
	 * @param loaderContext {@link ClassSectionLoaderContext}.
	 * @return <code>true</code> to include the {@link ManagedFunctionType}.
	 */
	public boolean isIncludeFunction(ManagedFunctionType<?, ?> functionType, ClassSectionLoaderContext loaderContext) {
		return true;
	}

	/**
	 * Obtains the {@link ManagedFunction} name from the
	 * {@link ManagedFunctionType}.
	 * 
	 * @param functionType  {@link ManagedFunctionType}.
	 * @param loaderContext {@link ClassSectionLoaderContext}.
	 * @return {@link ManagedFunction} name.
	 */
	public String getFunctionName(ManagedFunctionType<?, ?> functionType, ClassSectionLoaderContext loaderContext) {
		return functionType.getFunctionName();
	}

	/**
	 * Decorates the {@link SectionFunction}.
	 * 
	 * @param functionContext {@link FunctionClassSectionLoaderContext}.
	 */
	public void decorateSectionFunction(FunctionClassSectionLoaderContext functionContext) {
		// No decoration by default
	}

}
