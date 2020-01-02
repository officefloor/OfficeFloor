/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.scala;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.json.ObjectMapperResponderDecorator;
import net.officefloor.web.json.ObjectMapperResponderDecoratorServiceFactory;

/**
 * Scala {@link ObjectMapperResponderDecoratorServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ScalaObjectMapperResponderDecoratorServiceFactory
		implements ObjectMapperResponderDecoratorServiceFactory, ObjectMapperResponderDecorator {

	/*
	 * ============== ObjectMapperResponderDecoratorServiceFactory ==============
	 */

	@Override
	public ObjectMapperResponderDecorator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ====================== ObjectMapperResponderDecorator ======================
	 */

	@Override
	public void decorateObjectMapper(ObjectMapper mapper) throws Exception {
		mapper.registerModule(new DefaultScalaModule());
	}

}