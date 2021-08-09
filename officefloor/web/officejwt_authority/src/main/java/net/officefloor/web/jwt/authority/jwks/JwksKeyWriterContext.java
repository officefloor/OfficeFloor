/*-
 * #%L
 * JWT Authority
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.jwt.authority.jwks;

import java.math.BigInteger;
import java.security.Key;
import java.util.Base64;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * Context for the {@link JwksKeyWriter}.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwksKeyWriterContext<K> {

	/**
	 * Obtains the {@link Key} to write.
	 * 
	 * @return {@link Key} to write.
	 */
	K getKey();

	/**
	 * Obtains the key {@link ObjectNode} to be populated with the {@link Key}
	 * details.
	 * 
	 * @return Key {@link ObjectNode} to be populated with the {@link Key} details.
	 */
	ObjectNode getKeyNode();

	/**
	 * Obtains the {@link JsonNodeFactory}.
	 * 
	 * @return {@link JsonNodeFactory}.
	 */
	JsonNodeFactory getNodeFactory();

	/**
	 * Specifies the key type.
	 * 
	 * @param kty Key type.
	 */
	default void setKty(String kty) {
		this.setString("kty", kty);
	}

	/**
	 * Convenience method to set long value on key {@link ObjectNode}.
	 * 
	 * @param fieldName Field name.
	 * @param value     Value.
	 */
	default void setLong(String fieldName, long value) {
		this.setLong(this.getKeyNode(), fieldName, value);
	}

	/**
	 * Specifies the field long value on the {@link ObjectNode}.
	 * 
	 * @param node      {@link ObjectNode}.
	 * @param fieldName Field name.
	 * @param value     Value.
	 */
	default void setLong(ObjectNode node, String fieldName, long value) {
		NumericNode number = this.getNodeFactory().numberNode(value);
		node.set(fieldName, number);
	}

	/**
	 * Convenience method to set string value on key {@link ObjectNode}.
	 * 
	 * @param fieldName Field name.
	 * @param value     Value.
	 */
	default void setString(String fieldName, String value) {
		this.setString(this.getKeyNode(), fieldName, value);
	}

	/**
	 * Specifies the field string value on the {@link ObjectNode}.
	 * 
	 * @param node      {@link ObjectNode}.
	 * @param fieldName Field name.
	 * @param value     Value.
	 */
	default void setString(ObjectNode node, String fieldName, String value) {
		TextNode text = this.getNodeFactory().textNode(value);
		node.set(fieldName, text);
	}

	/**
	 * Convenience method to set {@link BigInteger} value as Base64 on key
	 * {@link ObjectNode}.
	 * 
	 * @param fieldName Field name.
	 * @param value     Value.
	 */
	default void setBase64(String fieldName, BigInteger value) {
		this.setBase64(this.getKeyNode(), fieldName, value);
	}

	/**
	 * Specifies the field {@link BigInteger} value as Base64 on the
	 * {@link ObjectNode}.
	 * 
	 * @param node      {@link ObjectNode}.
	 * @param fieldName Field name.
	 * @param value     Value.
	 */
	default void setBase64(ObjectNode node, String fieldName, BigInteger value) {
		byte[] bytes = value.toByteArray();
		String base64Value = Base64.getUrlEncoder().encodeToString(bytes);
		this.setString(node, fieldName, base64Value);
	}

	/**
	 * Convenience method to set bytes as Base64 on key {@link ObjectNode}.
	 * 
	 * @param fieldName Field name.
	 * @param value     Value.
	 */
	default void setBase64(String fieldName, byte[] value) {
		this.setBase64(this.getKeyNode(), fieldName, value);
	}

	/**
	 * Specifies the field bytes as Base64 on the {@link ObjectNode}.
	 * 
	 * @param node      {@link ObjectNode}.
	 * @param fieldName Field name.
	 * @param value     Value.
	 */
	default void setBase64(ObjectNode node, String fieldName, byte[] value) {
		String base64Value = Base64.getUrlEncoder().encodeToString(value);
		this.setString(node, fieldName, base64Value);
	}

}
