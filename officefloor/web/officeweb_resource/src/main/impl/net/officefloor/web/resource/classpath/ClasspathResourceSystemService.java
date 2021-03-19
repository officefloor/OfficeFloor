/*-
 * #%L
 * Web resources
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

package net.officefloor.web.resource.classpath;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.resource.spi.ResourceSystem;
import net.officefloor.web.resource.spi.ResourceSystemFactory;
import net.officefloor.web.resource.spi.ResourceSystemService;

/**
 * {@link ResourceSystemFactory} to create a {@link ResourceSystem} from the
 * class path.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathResourceSystemService implements ResourceSystemService {

	/*
	 * ====================== ResourceSystemService =======================
	 */

	@Override
	public ResourceSystemFactory createService(ServiceContext context) throws Throwable {
		return new ClasspathResourceSystemFactory(context.getClassLoader());
	}

}
