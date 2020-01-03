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