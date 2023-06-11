package net.officefloor.cabinet.common;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.FieldValueDeserialiser;
import net.officefloor.cabinet.common.adapt.FieldValueSetter;
import net.officefloor.cabinet.common.adapt.FieldValueTranslator;
import net.officefloor.cabinet.common.adapt.ScalarFieldValueGetter;

/**
 * Mock {@link AbstractDocumentAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockDocumentAdapter extends AbstractDocumentAdapter<Map<String, Object>, Map<String, Object>> {

	private static final Class<?>[] FIELD_TYPES = new Class[] { boolean.class, Boolean.class, byte.class, Byte.class,
			short.class, char.class, Character.class, Short.class, int.class, Integer.class, long.class, Long.class,
			float.class, Float.class, double.class, Double.class, String.class, Map.class };

	private Map<Class<?>, Function<String, ?>> fieldTypeDeserialisers;

	private <T> void addFieldTypeDeserialiser(Class<T> clazz, Function<String, T> deserialiser) {
		if (this.fieldTypeDeserialisers == null) {
			this.fieldTypeDeserialisers = new HashMap<>();
		}
		this.fieldTypeDeserialisers.put(clazz, deserialiser);
	}

	private <T> void addFieldTypeDeserialiser(Class<T> primitive, Class<T> boxed, Function<String, T> deserialiser) {
		this.addFieldTypeDeserialiser(primitive, deserialiser);
		this.addFieldTypeDeserialiser(boxed, deserialiser);
	}

	@SuppressWarnings("unchecked")
	public <V> FieldValueDeserialiser<V> fieldTypeSerialiseable(Class<?> clazz) {
		Function<String, ?> deserialiser = this.fieldTypeDeserialisers.get(clazz);
		if (deserialiser == null) {
			return notDeserialiseable(clazz);
		}

		// Handle deserialise
		return (fieldName, serialisedValue) -> {
			if (serialisedValue == null) {
				return null;
			}
			return (V) deserialiser.apply(serialisedValue);
		};
	}

	public MockDocumentAdapter(AbstractOfficeStore officeStore) {
		super(officeStore);
	}

	@Override
	protected void initialise(Initialise init) throws Exception {

		// Configure the field type deserialisers
		this.addFieldTypeDeserialiser(int.class, Integer.class, Integer::parseInt);
		this.addFieldTypeDeserialiser(String.class, (value) -> value);

		// Configure
		init.setInternalDocumentFactory(() -> new HashMap<>());
		init.setKeyGetter((document, keyName) -> (String) document.get(keyName));
		init.setKeySetter((document, keyName, keyValue) -> document.put(keyName, keyValue));
		for (Class<?> type : FIELD_TYPES) {
			init.addFieldType(type, getFieldValue(), getFieldTranslator(), getFieldSetter(), serialiser(),
					this.fieldTypeSerialiseable(type));
		}
		init.addFieldType(Map.class, getFieldValue(), getFieldTranslator(), getFieldSetter(), serialiser(),
				notDeserialiseable(Map.class));
	}

	private static <V> ScalarFieldValueGetter<Map<String, Object>, V> getFieldValue() {
		return new ScalarFieldValueGetter<Map<String, Object>, V>() {
			@Override
			public V getValue(Map<String, Object> internalDocument, String fieldName) {
				return (V) internalDocument.get(fieldName);
			}
		};
	}

	private static <V> FieldValueTranslator<V, V> getFieldTranslator() {
		return new FieldValueTranslator<V, V>() {
			@Override
			public V translate(String fieldName, V fieldValue) {
				return fieldValue;
			}
		};
	}

	private static <V> FieldValueSetter<Map<String, Object>, V> getFieldSetter() {
		return new FieldValueSetter<Map<String, Object>, V>() {
			@Override
			public void setValue(Map<String, Object> internalDocument, String fieldName, V value,
					CabinetManagerChange change) {
				internalDocument.put(fieldName, value);
			}
		};
	}

}
