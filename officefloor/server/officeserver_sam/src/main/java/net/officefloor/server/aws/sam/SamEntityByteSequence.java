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