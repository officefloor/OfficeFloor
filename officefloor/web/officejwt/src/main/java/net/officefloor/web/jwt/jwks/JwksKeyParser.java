package net.officefloor.web.jwt.jwks;

import java.math.BigInteger;
import java.security.Key;
import java.util.Base64;
import java.util.function.Function;

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
	 * @param keyNode {@link JsonNode} to the <code>key</code>.
	 * @return {@link Key} or <code>null</code> if not able to parse {@link Key}
	 *         (indicating for another {@link JwksKeyParser} to attempt to obtain
	 *         the {@link Key}).
	 * @throws Exception If failure parsing the {@link Key}.
	 */
	Key parseKey(JsonNode keyNode) throws Exception;

	/*
	 * Convenience methods to extra values from JSON node.
	 */

	/**
	 * Key type.
	 */
	static String KTY = "kty";

	/**
	 * Obtains the field long value.
	 * 
	 * @param node         {@link JsonNode}.
	 * @param fieldName    Field name.
	 * @param defaultValue Default value.
	 * @return Field long value.
	 */
	static Long getLong(JsonNode node, String fieldName, Long defaultValue) {
		return getValue(node, fieldName, defaultValue, (field) -> field.asLong(defaultValue));
	}

	/**
	 * Obtains the field string value.
	 * 
	 * @param node         {@link JsonNode}.
	 * @param fieldName    Field name.
	 * @param defaultValue Default value.
	 * @return Field string value.
	 */
	static String getString(JsonNode node, String fieldName, String defaultValue) {
		return getValue(node, fieldName, defaultValue, (field) -> field.asText(defaultValue));
	}

	/**
	 * Obtains the field {@link BigInteger} value.
	 * 
	 * @param node         {@link JsonNode}.
	 * @param fieldName    Field name.
	 * @param defaultValue Default value.
	 * @return Field {@link BigInteger} value.
	 */
	static BigInteger getBase64BigInteger(JsonNode node, String fieldName, BigInteger defaultValue) {
		return getValue(node, fieldName, defaultValue, (field) -> {
			String base64Value = field.asText();
			byte[] bytes = Base64.getUrlDecoder().decode(base64Value);
			return new BigInteger(bytes);
		});
	}

	/**
	 * Obtains the field value from the {@link JsonNode}.
	 * 
	 * @param node         {@link JsonNode}.
	 * @param fieldName    Field name.
	 * @param defaultValue Default value. May be <code>null</code>.
	 * @param getValue     Obtains the value from the {@link JsonNode} field.
	 * @return Field value from the {@link JsonNode}.
	 */
	static <T> T getValue(JsonNode node, String fieldName, T defaultValue, Function<JsonNode, T> getValue) {
		JsonNode field = node.get(fieldName);
		if (field == null) {
			return defaultValue;
		}
		return getValue.apply(field);
	}

}