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
package net.officefloor.web.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import net.officefloor.frame.api.source.ServiceContext;

/**
 * Mock {@link ObjectMapperDecorator} for testing decorating the
 * {@link ObjectMapper}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockObjectMapperDecorator implements ObjectMapperDecoratorServiceFactory, ObjectMapperDecorator {

	/**
	 * Indicates whether to decorate the {@link ObjectMapper}.
	 */
	public static boolean isDecorate = false;

	/*
	 * ===================== ObjectMapperDecoratorServiceFactory ==================
	 */

	@Override
	public ObjectMapperDecorator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================= ObjectMapperDecorator ===============================
	 */

	@Override
	public void decorateObjectMapper(ObjectMapper mapper) throws Exception {
		mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

}