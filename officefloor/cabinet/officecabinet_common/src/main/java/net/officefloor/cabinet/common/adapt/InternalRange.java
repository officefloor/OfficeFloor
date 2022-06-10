package net.officefloor.cabinet.common.adapt;

import java.util.Map;

import net.officefloor.cabinet.common.AbstractOfficeCabinet;
import net.officefloor.cabinet.spi.Range;
import net.officefloor.cabinet.spi.Range.Direction;

/**
 * Internal {@link Range}.
 * 
 * @author Daniel Sagenschneider
 */
public class InternalRange {

	private final String fieldName;

	private final Direction direction;

	private final int limit;

	private final String nextDocumentBundleToken;

	private final AbstractOfficeCabinet<?, ?, ?, ?> cabinet;

	private Map<String, String> nextDocumentBundleTokenValues = null;

	public InternalRange(String fieldName, Direction direction, int limit, String nextDocumentBundleToken,
			AbstractOfficeCabinet<?, ?, ?, ?> cabinet) {
		this.fieldName = fieldName;
		this.direction = direction;
		this.limit = limit;
		this.nextDocumentBundleToken = nextDocumentBundleToken;
		this.cabinet = cabinet;
	}

	public String getFieldName() {
		return fieldName;
	}

	public Direction getDirection() {
		return direction;
	}

	public int getLimit() {
		return limit;
	}

	public String getNextDocumentBundleToken() {
		return this.nextDocumentBundleToken;
	}

	/**
	 * Obtains the key value from the token.
	 * 
	 * @return Key value from the token.
	 */
	public String getTokenKeyValue() {

		// Obtain the token values
		Map<String, String> values = this.getTokenValues();

		// Obtain the key value
		String keyValue = values.get(this.cabinet.getKeyName());

		// Return the key value
		return keyValue;
	}

	@SuppressWarnings("unchecked")
	public <V> V getTokenFieldValue(String fieldName) {

		// Obtain the token values
		Map<String, String> values = this.getTokenValues();

		// Obtain the field serialised value
		String serialisedValue = values.get(fieldName);

		// Obtain the deserialised value
		Object value = this.cabinet.getDeserialisedFieldValue(fieldName, serialisedValue);

		// Return the value
		return (V) value;
	}

	private Map<String, String> getTokenValues() {

		// Obtain the token values
		Map<String, String> values = this.nextDocumentBundleTokenValues;
		if (values == null) {
			values = this.cabinet.deserialiseNextDocumentToken(this.nextDocumentBundleToken);

			// Cache to avoid deserialising each field
			this.nextDocumentBundleTokenValues = values;
		}

		// Return the values
		return values;
	}
}