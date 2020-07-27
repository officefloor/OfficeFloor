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
