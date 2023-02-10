package net.officefloor.cabinet.common;

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.officefloor.cabinet.AttributeTypesDocument;
import net.officefloor.cabinet.HierarchicalDocument;
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
public class MockDocumentAdapter<D> extends AbstractDocumentAdapter<D, D> {

	private static final Class<?>[] FIELD_TYPES = new Class[] { boolean.class, Boolean.class, byte.class, Byte.class,
			short.class, char.class, Character.class, Short.class, int.class, Integer.class, long.class, Long.class,
			float.class, Float.class, double.class, Double.class, String.class, Map.class };

	private final Class<D> documentType;

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

	public MockDocumentAdapter(Class<D> documentType, AbstractOfficeStore officeStore) {
		super(officeStore);
		this.documentType = documentType;
	}

	@Override
	protected void initialise(Initialise init) throws Exception {

		// Configure the field type deserialisers
		this.addFieldTypeDeserialiser(int.class, Integer.class, Integer::parseInt);
		this.addFieldTypeDeserialiser(String.class, (value) -> value);

		// Configure
		init.setInternalDocumentFactory(() -> newDocument(this.documentType));
		init.setKeyGetter((document, keyName) -> getValue(document, keyName));
		init.setKeySetter((document, keyName, keyValue) -> setValue(document, keyName, keyValue));
		for (Class<?> type : FIELD_TYPES) {
			init.addFieldType(type, getFieldValue(), getFieldTranslator(), getFieldSetter(), serialiser(),
					this.fieldTypeSerialiseable(type));
		}
		init.addFieldType(Map.class, getMapFieldValue(), getFieldTranslator(), getMapFieldSetter(), serialiser(),
				notDeserialiseable(Map.class));
	}

	private static <D> D newDocument(Class<D> documentType) {
		try {
			return documentType.getConstructor().newInstance();
		} catch (Exception e) {
			return fail("Unable to create document of type " + documentType.getName());
		}
	}

	public static <D, V> V getValue(D document, String fieldName) {
		Field field = getField(document, fieldName);
		return getValue(document, field);
	}

	@SuppressWarnings("unchecked")
	private static <D, V> V getValue(D document, Field field) {
		Object fieldValue;
		try {
			field.setAccessible(true);
			fieldValue = field.get(document);
		} catch (Exception ex) {
			return fail(
					"Unable to get field " + field.getName() + " from document type " + document.getClass().getName(),
					ex);
		}
		return (V) fieldValue;
	}

	public static <D, V> void setValue(D document, String fieldName, V value) {
		Field field = getField(document, fieldName);
		try {
			field.setAccessible(true);
			field.set(document, value);
		} catch (Exception ex) {
			fail("Unable to set " + fieldName + " on document type " + document.getClass().getName(), ex);
		}
	}

	public static <D> Field getField(D document, String fieldName) {

		// Search for field
		Class<?> clazz = document.getClass();
		while (clazz != null) {
			for (Field field : clazz.getDeclaredFields()) {
				if (fieldName.equals(field.getName())) {
					field.setAccessible(true);
					return field;
				}
			}
			clazz = clazz.getSuperclass();
		}

		// Should always find field
		return fail("No field " + fieldName + " on document type " + document.getClass().getName());
	}

	private static <D, V> ScalarFieldValueGetter<D, V> getFieldValue() {
		return new ScalarFieldValueGetter<D, V>() {
			@Override
			public V getValue(D internalDocument, String fieldName) {
				return MockDocumentAdapter.getValue(internalDocument, fieldName);
			}
		};
	}

	@SuppressWarnings("rawtypes")
	private static <D> ScalarFieldValueGetter<D, Map> getMapFieldValue() {
		return new ScalarFieldValueGetter<D, Map>() {
			@Override
			public Map getValue(D internalDocument, String fieldName) {

				// Handle hierarchical document child
				if (internalDocument instanceof HierarchicalDocument) {

					// Obtain the child
					Object child = MockDocumentAdapter.getValue(internalDocument, fieldName);
					if (child == null) {
						return null; // no child
					}

					// Obtain mapping of fields
					Map<String, Object> data = new HashMap<>();
					Class<?> childType = child.getClass();
					do {
						// Load field values of the object
						for (Field childField : childType.getDeclaredFields()) {
							String childFieldName = childField.getName();
							Object childFieldValue = MockDocumentAdapter.getValue(child, childField);

							// Handle character
							Class<?> childFieldType = childField.getType();
							if (Character.class.isAssignableFrom(childFieldType)
									|| char.class.isAssignableFrom(childFieldType)) {
								Character childFieldCharacterValue = (Character) childFieldValue;
								childFieldValue = childFieldValue != null ? String.valueOf(childFieldCharacterValue)
										: null;
							}

							// Load the data
							data.put(childFieldName, childFieldValue);
						}

						childType = childType.getSuperclass();
					} while (childType != null);

					// Return the data for child object
					return data;

				} else {
					// Obtain raw child object
					return MockDocumentAdapter.getValue(internalDocument, fieldName);
				}
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

	private static <D, V> FieldValueSetter<D, V> getFieldSetter() {
		return new FieldValueSetter<D, V>() {
			@Override
			public void setValue(D internalDocument, String fieldName, V value) {
				MockDocumentAdapter.setValue(internalDocument, fieldName, value);
			}
		};
	}

	@SuppressWarnings("rawtypes")
	private static <D> FieldValueSetter<D, Map> getMapFieldSetter() {
		return new FieldValueSetter<D, Map>() {

			@Override
			@SuppressWarnings("unchecked")
			public void setValue(D internalDocument, String fieldName, Map value) {

				// Handle hierarchical document child
				if ((value != null) && (internalDocument instanceof HierarchicalDocument)) {
					// Provide child
					AttributeTypesDocument child = new AttributeTypesDocument();
					Map<String, Object> data = (Map<String, Object>) value;
					for (String childFieldName : data.keySet()) {
						Object childFieldValue = data.get(childFieldName);

						// Handle character
						Field childField = MockDocumentAdapter.getField(child, childFieldName);
						Class<?> childFieldType = childField.getType();
						if (Character.class.isAssignableFrom(childFieldType)
								|| char.class.isAssignableFrom(childFieldType)) {
							String childFieldStringValue = (String) childFieldValue;
							childFieldValue = childFieldValue != null ? childFieldStringValue.charAt(0) : null;
						}

						// Load child value
						MockDocumentAdapter.setValue(child, childFieldName, childFieldValue);
					}
					MockDocumentAdapter.setValue(internalDocument, fieldName, child);

				} else {
					// Map on document
					getFieldSetter().setValue(internalDocument, fieldName, value);
				}
			}
		};
	}

}
