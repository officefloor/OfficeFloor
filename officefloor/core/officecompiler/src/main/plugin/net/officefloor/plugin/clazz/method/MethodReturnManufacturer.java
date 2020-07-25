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

package net.officefloor.plugin.clazz.method;

import java.lang.reflect.Method;

/**
 * Manufactures the {@link MethodReturnTranslator}.
 * 
 * @param <R> {@link MethodFunction} return type.
 * @param <T> Translated type.
 * 
 * @author Daniel Sagenschneider
 */
public interface MethodReturnManufacturer<R, T> {

	/**
	 * <p>
	 * Creates the {@link MethodReturnTranslator} for the particular {@link Method}
	 * return.
	 * <p>
	 * Should the {@link MethodReturnManufacturer} not handle the return value, it
	 * should return <code>null</code>. This is because the first
	 * {@link MethodReturnManufacturer} providing a {@link MethodReturnTranslator}
	 * will be used.
	 * 
	 * @param context {@link MethodReturnManufacturerContext}.
	 * @return {@link MethodReturnTranslator} or <code>null</code> if not able to
	 *         handle return value.
	 * @throws Exception If fails to create the {@link MethodReturnTranslator}.
	 */
	MethodReturnTranslator<R, T> createReturnTranslator(MethodReturnManufacturerContext<T> context) throws Exception;

}
