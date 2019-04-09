package net.officefloor.web.jwt.authority.jwks;

import java.security.Key;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Creates the JWKS <code>key</code> {@link JsonNode} from the {@link Key}.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwksKeyWriter<K extends Key> {

	/**
	 * Indicates if able to write the {@link Key}.
	 * 
	 * @param key {@link Key}.
	 * @return <code>true</code> if able to write the {@link Key}.
	 */
	boolean canWriteKey(Key key);

	/**
	 * Writes the {@link Key} as {@link ObjectNode}.
	 * 
	 * @param context {@link JwksKeyWriterContext}.
	 * @throws Exception If fails to write the {@link Key}.
	 */
	void writeKey(JwksKeyWriterContext<K> context) throws Exception;

}