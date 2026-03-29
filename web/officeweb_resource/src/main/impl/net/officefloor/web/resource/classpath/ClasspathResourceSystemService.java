/*-
 * #%L
 * Web resources
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
