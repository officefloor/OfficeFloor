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