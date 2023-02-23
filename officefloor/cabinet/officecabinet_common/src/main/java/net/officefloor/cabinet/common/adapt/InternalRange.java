package net.officefloor.cabinet.common.adapt;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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

	private final StartAfterDocumentValueGetter startAfterDocumentValueGetter;

	private Map<String, Object> nextDocumentBundleTokenValues = null;

	public InternalRange(String fieldName, Direction direction, int limit, String nextDocumentBundleToken,
			AbstractOfficeCabinet<?, ?, ?, ?> cabinet) {
		this.fieldName = fieldName;
		this.direction = direction;
		this.limit = limit;
		this.nextDocumentBundleToken = nextDocumentBundleToken;
		this.startAfterDocumentValueGetter = (nextDocumentBundleToken != null) ? new StartAfterDocumentValueGetterImpl()
				: null;
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
		Map<String, Object> values = this.getTokenValues();
		if (values == null) {
			return null; // no token values
		}

		// Obtain the key value
		Object keyValue = values.get(this.cabinet.getKeyName());

		// Return the key value
		return (String) keyValue;
	}

	@SuppressWarnings("unchecked")
	public <V> V getTokenFieldValue(String fieldName) {

		// Obtain the token values
		Map<String, Object> values = this.getTokenValues();
		if (values == null) {
			return null; // no token values
		}

		// Obtain the value
		Object value = values.get(fieldName);

		// Return the value
		return (V) value;
	}

	public Map<String, Object> getTokenValues() {

		// No token values if no token
		if (this.nextDocumentBundleToken == null) {
			return null;
		}

		// Obtain the token values
		Map<String, Object> values = this.nextDocumentBundleTokenValues;
		if (values == null) {

			// Obtain the serialised values
			Map<String, String> serialisedValues = this.cabinet
					.deserialiseNextDocumentToken(this.nextDocumentBundleToken);

			// Transform to deserialised values
			values = new HashMap<>(serialisedValues.size());
			for (Entry<String, String> entry : serialisedValues.entrySet()) {
				String key = entry.getKey();
				String serialisedValue = entry.getValue();
				Object deserialisedValue = this.cabinet.getDeserialisedFieldValue(key, serialisedValue);
				values.put(key, deserialisedValue);
			}

			// Cache to avoid deserialising again
			this.nextDocumentBundleTokenValues = values;
		}

		// Return the values
		return values;
	}

	public StartAfterDocumentValueGetter getStartAfterDocumentValueGetter() {
		return this.startAfterDocumentValueGetter;
	}

	/**
	 * {@link StartAfterDocumentValueGetter} implementation.
	 */
	private class StartAfterDocumentValueGetterImpl implements StartAfterDocumentValueGetter {

		@Override
		public String getKeyFieldName() {
			return InternalRange.this.cabinet.getKeyName();
		}

		@Override
		public String getKey() {
			return InternalRange.this.getTokenKeyValue();
		}

		@Override
		public Object getValue(String fieldName) {
			return InternalRange.this.getTokenFieldValue(fieldName);
		}
	}

}