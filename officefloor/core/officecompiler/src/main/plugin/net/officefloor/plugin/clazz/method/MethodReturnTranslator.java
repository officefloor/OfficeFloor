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

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Translate the return value of the {@link MethodFunction}.
 * 
 * @param <R> {@link MethodFunction} return type.
 * @param <T> Translated type.
 * 
 * @author Daniel Sagenschneider
 */
public interface MethodReturnTranslator<R, T> {

	/**
	 * Translates the {@link Method} return value for next {@link ManagedFunction}
	 * argument.
	 * 
	 * @param context {@link MethodReturnTranslatorContext}.
	 * @throws Exception If fails to translate.
	 */
	void translate(MethodReturnTranslatorContext<R, T> context) throws Exception;

}
