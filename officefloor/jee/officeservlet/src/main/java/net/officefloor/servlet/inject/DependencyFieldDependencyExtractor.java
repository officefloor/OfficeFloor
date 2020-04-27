/*-
 * #%L
 * Servlet
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

package net.officefloor.servlet.inject;

import java.lang.reflect.Field;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.managedobject.clazz.Dependency;

/**
 * {@link Dependency} {@link FieldDependencyExtractor}.
 * 
 * @author Daniel Sagenschneider
 */
public class DependencyFieldDependencyExtractor
		implements FieldDependencyExtractor, FieldDependencyExtractorServiceFactory {

	/*
	 * =================== FieldDependencyExtractorServiceFactory =================
	 */

	@Override
	public FieldDependencyExtractor createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public RequiredDependency extractRequiredDependency(Field field) {
		return field.isAnnotationPresent(Dependency.class) ? new RequiredDependency(null, field.getType()) : null;
	}

}
