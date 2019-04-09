/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.web.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.ServiceFactory;
import net.officefloor.web.build.HttpObjectParserFactory;
import net.officefloor.web.build.HttpObjectParserServiceFactory;

/**
 * {@link ServiceFactory} for Jackson {@link HttpObjectParserServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class JacksonHttpObjectParserServiceFactory implements HttpObjectParserServiceFactory {

	/*
	 * ==================== ServiceFactory ===================
	 */

	@Override
	public HttpObjectParserFactory createService(ServiceContext context) throws Throwable {

		// Create the Object Mapper
		ObjectMapper mapper = new ObjectMapper();

		// Decorate the Object Mapper
		for (ObjectMapperParserDecorator decorator : context
				.loadOptionalServices(ObjectMapperParserDecoratorServiceFactory.class)) {
			decorator.decorateObjectMapper(mapper);
		}

		// Return factory
		return new JacksonHttpObjectParserFactory(mapper);
	}

}