/*-
 * #%L
 * Web Plug-in
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
