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