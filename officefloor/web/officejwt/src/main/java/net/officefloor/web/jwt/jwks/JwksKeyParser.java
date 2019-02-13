package net.officefloor.web.jwt.jwks;

import java.security.Key;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Parses out the JWKS {@link Key} from the content.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwksKeyParser {

	/**
	 * Parses the {@link Key} from the JWK <code>key</code> {@link JsonNode}.
	 * 
	 * @param context {@link JwksKeyParserContext}.
	 * @return {@link Key} or <code>null</code> if not able to parse {@link Key}
	 *         (indicating for another {@link JwksKeyParser} to attempt to obtain
	 *         the {@link Key}).
	 * @throws Exception If failure parsing the {@link Key}.
	 */
	Key parseKey(JwksKeyParserContext context) throws Exception;

}