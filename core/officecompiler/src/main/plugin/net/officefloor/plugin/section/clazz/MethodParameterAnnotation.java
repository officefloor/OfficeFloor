/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
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

package net.officefloor.plugin.section.clazz;

import java.lang.reflect.Method;

/**
 * Annotates the {@link net.officefloor.frame.api.function.ManagedFunction} object with
 * the details of the parameter for the {@link MethodAnnotation}.
 */
public interface MethodParameterAnnotation {

    /**
     * Obtains the {@link Method}.
     *
     * @return {@link Method}.
     */
    Method getMethod();

    /**
     * Obtains the index of the parameter on the {@link java.lang.reflect.Method}.
     *
     * @return Index of the parameter on the {@link java.lang.reflect.Method}.
     */
    int getParameterIndex();
}
