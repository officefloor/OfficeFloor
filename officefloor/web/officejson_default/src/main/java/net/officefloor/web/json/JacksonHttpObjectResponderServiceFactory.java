/*-
 * #%L
 * JSON default for Web
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

package net.officefloor.web.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.build.HttpObjectResponderFactory;
import net.officefloor.web.build.HttpObjectResponderServiceFactory;

/**
 * Jackson {@link HttpObjectResponderServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class JacksonHttpObjectResponderServiceFactory implements HttpObjectResponderServiceFactory {

	/*
	 * =============== HttpObjectResponderServiceFactory ==============
	 */

	@Override
	public HttpObjectResponderFactory createService(ServiceContext context) throws Throwable {

		// Create the Object Mapper
		ObjectMapper mapper = new ObjectMapper();

		// Decorate the Object Mapper
		for (ObjectMapperResponderDecorator decorator : context
				.loadOptionalServices(ObjectMapperResponderDecoratorServiceFactory.class)) {
			decorator.decorateObjectMapper(mapper);
		}

		// Return factory
		return new JacksonHttpObjectResponderFactory(mapper);
	}

}
