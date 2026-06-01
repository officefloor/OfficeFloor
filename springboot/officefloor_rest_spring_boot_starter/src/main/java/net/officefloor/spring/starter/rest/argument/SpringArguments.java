/*-
 * #%L
 * OfficeFloor REST Spring Boot Starter
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

package net.officefloor.spring.starter.rest.argument;

import net.officefloor.frame.api.source.SourceContext;

/** Provides Spring argument annotation types. */
public interface SpringArguments {

    /**
     * Listing of annotation types for Spring arguments.
     *
     * @param context {@link SourceContext}.
     * @return Annotation types for Spring arguments.
     */
    Class<?>[] getArgumentAnnotationTypes(SourceContext context);

    /**
     * Listing of types for Spring arguments.
     *
     * @param context {@link SourceContext}.
     * @return Types for Spring arguments.
     */
    Class<?>[] getArgumentTypes(SourceContext context);
}
