/*-
 * #%L
 * JSON default for Web
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

package net.officefloor.web.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.frame.api.source.ServiceContext;

/**
 * Mock {@link ObjectMapperParserDecorator} for testing decorating the
 * {@link ObjectMapper}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockObjectMapperParserDecorator
		implements ObjectMapperParserDecoratorServiceFactory, ObjectMapperParserDecorator {

	/**
	 * Indicates whether to decorate the {@link ObjectMapper}.
	 */
	public static boolean isDecorate = false;

	/*
	 * ===================== ObjectMapperDecoratorServiceFactory ==================
	 */

	@Override
	public ObjectMapperParserDecorator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================= ObjectMapperDecorator ===============================
	 */

	@Override
	public void decorateObjectMapper(ObjectMapper mapper) throws Exception {
		if (isDecorate) {
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		}
	}

}
