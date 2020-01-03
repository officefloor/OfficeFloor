package net.officefloor.web.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import net.officefloor.frame.api.source.ServiceContext;

/**
 * Mock {@link ObjectMapperResponderDecorator} for testing decorating the
 * {@link ObjectMapper}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockObjectMapperResponderDecorator
		implements ObjectMapperResponderDecoratorServiceFactory, ObjectMapperResponderDecorator {

	/**
	 * Indicates whether to decorate the {@link ObjectMapper}.
	 */
	public static boolean isDecorate = false;

	/*
	 * ===================== ObjectMapperDecoratorServiceFactory ==================
	 */

	@Override
	public ObjectMapperResponderDecorator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================= ObjectMapperDecorator ===============================
	 */

	@Override
	public void decorateObjectMapper(ObjectMapper mapper) throws Exception {
		if (isDecorate) {
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
		}
	}

}