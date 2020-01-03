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