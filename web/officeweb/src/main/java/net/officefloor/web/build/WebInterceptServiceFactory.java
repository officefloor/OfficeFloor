/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.build;

import java.lang.reflect.Method;

import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.frame.api.source.ServiceFactory;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpRequest;

/**
 * <p>
 * {@link ServiceFactory} to provide a single {@link Method} {@link Class} for
 * intercepting all {@link HttpRequest} instances before they are serviced by
 * WoOF.
 * <p>
 * This is typically useful for adding additional non-application logic
 * information to responses, such as CORS headers.
 * <p>
 * The {@link Class} is loaded with a {@link ClassSectionSource} and must have
 * only one {@link SectionInput} and one {@link SectionOutput}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebInterceptServiceFactory extends ServiceFactory<Class<?>> {
}
