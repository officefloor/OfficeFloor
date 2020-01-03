package net.officefloor.web.json;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Decorates the {@link ObjectMapper} for the
 * {@link JacksonHttpObjectParserFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ObjectMapperParserDecorator {

	/**
	 * Decorates the {@link ObjectMapper}.
	 * 
	 * @param mapper {@link ObjectMapper}.
	 * @throws Exception If fails to decorate the {@link ObjectMapper}.
	 */
	void decorateObjectMapper(ObjectMapper mapper) throws Exception;

}