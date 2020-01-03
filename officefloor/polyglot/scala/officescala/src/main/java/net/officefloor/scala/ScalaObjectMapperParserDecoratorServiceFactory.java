/*-
 * #%L
 * Scala
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

package net.officefloor.scala;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.json.ObjectMapperParserDecorator;
import net.officefloor.web.json.ObjectMapperParserDecoratorServiceFactory;

/**
 * Scala {@link ObjectMapperParserDecoratorServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ScalaObjectMapperParserDecoratorServiceFactory
		implements ObjectMapperParserDecoratorServiceFactory, ObjectMapperParserDecorator {

	/*
	 * ================ ObjectMapperParserDecoratorServiceFactory ================
	 */

	@Override
	public ObjectMapperParserDecorator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================== ObjectMapperParserDecorator =======================
	 */

	@Override
	public void decorateObjectMapper(ObjectMapper mapper) throws Exception {
		mapper.registerModule(new DefaultScalaModule());
	}

}
