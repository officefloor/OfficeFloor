/*-
 * #%L
 * Scala
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
