/*-
 * #%L
 * AWS SAM HTTP Server
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.server.aws.sam;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Base64.Decoder;

import net.officefloor.server.stream.impl.ByteSequence;

/**
 * {@link ByteSequence} for the AWS SAM entity.
 * 
 * @author Daniel Sagenschneider
 */
public class SamEntityByteSequence implements ByteSequence {

	/**
	 * UTF 8 {@link Charset}.
	 */
	private static final Charset UTF8 = Charset.forName("UTF-8");

	/**
	 * Base64 {@link Decoder}.
	 */
	private static final Decoder base64Decoder = Base64.getDecoder();

	/**
	 * Entity bytes.
	 */
	private final byte[] bytes;

	/**
	 * Instantiate.
	 * 
	 * @param entity          Entity.
	 * @param isBase64Encoded Indicates if entity is Base64 encoded.
	 */
	public SamEntityByteSequence(String entity, boolean isBase64Encoded) {
		if (entity == null) {
			this.bytes = new byte[0];
		} else if (isBase64Encoded) {
			this.bytes = base64Decoder.decode(entity);
		} else {
			this.bytes = entity.getBytes(UTF8);
		}
	}

	/*
	 * ==================== ByteSequence ======================
	 */

	@Override
	public byte byteAt(int index) {
		return this.bytes[index];
	}

	@Override
	public int length() {
		return this.bytes.length;
	}

}
