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