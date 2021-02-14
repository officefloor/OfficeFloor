/*-
 * #%L
 * AWS SAM HTTP Server
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
