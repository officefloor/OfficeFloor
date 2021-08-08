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
