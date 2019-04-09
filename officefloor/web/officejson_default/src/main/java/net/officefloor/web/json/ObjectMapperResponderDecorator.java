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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Decorates the {@link ObjectMapper} for the
 * {@link JacksonHttpObjectResponderFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ObjectMapperResponderDecorator {

	/**
	 * Decorates the {@link ObjectMapper}.
	 * 
	 * @param mapper {@link ObjectMapper}.
	 * @throws Exception If fails to decorate the {@link ObjectMapper}.
	 */
	void decorateObjectMapper(ObjectMapper mapper) throws Exception;

}